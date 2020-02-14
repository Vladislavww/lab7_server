package task7;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class UserClass {
	private String login;
	private String password;
	private String ip;
	private int port;
	private boolean isOnline;
	private Timer onlineActionTimer = new Timer(2000, new ActionListener(){
		public void actionPerformed(ActionEvent ev){
			set_online(false);
		}
	});
	
	public UserClass(String new_login, String new_password, String new_ip, int new_port){
		login = new_login;
		password = new_password;
		ip = new_ip;
		port = new_port;
		isOnline = false;
	}
	
	public String get_login(){
		return login;
	}
	
	public String get_password(){
		return password;
	}
	
	public String get_ip(){
		return ip;
	}
	
	public int get_port(){
		return port;
	}
	
	public void set_online(boolean condition){
		if(condition == true){
			isOnline = true;
			onlineActionTimer.start();
		}
		else{
			isOnline = false;
			onlineActionTimer.stop();
		}
	}
	
	public boolean get_online(){
		return isOnline;
	}

}