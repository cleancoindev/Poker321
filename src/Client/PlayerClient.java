package Client;

import java.rmi.Naming;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

import GUI.ServerListener;
import GUI.InitiateGameDisplayThread;
import GUI.UpdateAfterRoundThread;
import GUI.UpdateDuringRoundThread;
import Server.gameModule.RemoteGame;
import Server.userModule.RemoteUser;

public class PlayerClient extends UnicastRemoteObject  implements IPlayerClient {

	private RemoteGame rmGame;
	private int userId;
	private int gameId;
	
	Semaphore semA, semB;
	
	private ServerListener listener;

	public PlayerClient(int userId, RemoteGame rmGame, ServerListener listener) throws RemoteException {
		this.userId = userId;
		this.rmGame = rmGame;
		this.listener = listener;
		
		semA = new Semaphore(1);
		try {
			semA.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		semB = new Semaphore(1);
		try {
			semB.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Looks up the server object
	// Registers itself
	// Invokes the business methods on the server object:
	public static PlayerClient createPlayerProxy (int userId, RemoteGame rmGame, ServerListener listener)
			throws RemoteException {

		PlayerClient playerCl = new PlayerClient(userId, rmGame, listener);

		playerCl.rmGame.registerPlayer((IPlayerClient)playerCl);
	
		return playerCl;

	}
	
	public void InitiateGameDisplay()  throws RemoteException{
		
		System.out.println("Call back for game init");
		(new InitiateGameDisplayThread(listener)).start();
		//listener.addInGameConsoleMessage("Welcome to the game!");
		
		semA.release();
	}
	
	public void updateDuringRound(String msg)  throws RemoteException{
		
		try {
			semA.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		semA.release();
		
		(new UpdateDuringRoundThread(listener, rmGame, userId, msg)).start();
		
		
		semB.release();
	}
	
	public void updateAfterRound(String msg)  throws RemoteException{
		
		try {
			semB.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Now in playerclient, update after round");
		(new UpdateAfterRoundThread(listener, msg)).start();
		
	}
	
	public void getChatMessage(String from, String message)throws RemoteException{
		listener.addChatMessage(from, message);
	}

	// Getters
	public RemoteGame getGameProxy() {
		return rmGame;
	}

	public int getUserId() throws RemoteException{
		return userId;
	}

	public int getGameId() {
		return gameId;
	}
	
	public void setListener(ServerListener listener){
		this.listener = listener;
	}
	
	public int foo() throws RemoteException{
		System.out.println("FOOOOOOO"); 
		return 123;
	}
}
