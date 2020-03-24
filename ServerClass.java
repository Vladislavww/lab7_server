package task7;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class ServerClass {
	private static final int SERVER_PORT = 4512;
	private static ArrayList<UserClass> users = new ArrayList<UserClass>(5);
	private static String message;
	
	private static void writeDatabase() throws IOException{ //функция записи записи в файл
		FileWriter output_logins = new FileWriter("Logins.txt");
		FileWriter output_passwords = new FileWriter("Passwords.txt");
		FileWriter output_ip = new FileWriter("IP.txt");
		FileWriter output_ports = new FileWriter("Ports.txt");
		for(int i=0; i<users.size(); i++){
			output_logins.write(users.get(i).get_login()+'\n');
			output_passwords.write(users.get(i).get_password()+'\n');
			output_ip.write(users.get(i).get_ip()+'\n');
			Integer port = users.get(i).get_port();
			output_ports.write(port.toString()+'\n');
		}
		output_logins.close();
		output_passwords.close();
		output_ip.close();
		output_ports.close();
	}
	
	private static void readDatabase() throws IOException{ //функция чтения файла
		FileReader input_logins = new FileReader("Logins.txt");
		FileReader input_passwords = new FileReader("Passwords.txt");
		FileReader input_ip = new FileReader("IP.txt");
		FileReader input_ports = new FileReader("Ports.txt");
		Scanner scan_logins = new Scanner(input_logins);
		Scanner scan_passwords = new Scanner(input_passwords);
		Scanner scan_ip = new Scanner(input_ip);
		Scanner scan_ports = new Scanner(input_ports);
		while(scan_logins.hasNextLine()&&scan_passwords.hasNextLine()&&scan_ip.hasNextLine()&&scan_ports.hasNextLine()){
			users.add(new UserClass(scan_logins.nextLine(), scan_passwords.nextLine()));
		}
		input_logins.close();
		input_passwords.close();
		input_ip.close();
		input_ports.close();
	}
	
	private static int SearchUser(String name){
		for(int i=0; i<users.size(); i++){
			if(name.equals(users.get(i).get_login())){
				return i;
			}
		}
		return -1;
	}
	
	public static void main(String[] args) throws IOException {
		readDatabase();
		final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
		try{
			while(true){
				final Socket socket = serverSocket.accept();
				try{
					final DataInputStream in = new DataInputStream(socket.getInputStream());
					final String work_type = in.readUTF(); //режим работы сервера на данной итерации
					if(work_type.equals("CHECK_IN")){//режим проверки логина и пароля
						final String login = in.readUTF();
						final String password = in.readUTF();
						final String port = in.readUTF();
						message = "false";
						for(int i=0; i<users.size(); i++){
							if(login.equals(users.get(i).get_login()) && password.equals(users.get(i).get_password())){
								message = "true";
								users.get(i).set_ip(((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress());
								users.get(i).set_port(Integer.parseInt(port));
								break;
							}
						}
						final String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
						final Socket socket_out = new Socket(address, Integer.parseInt(port));
						final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						out.writeUTF(work_type);
						out.writeUTF(message);
						socket_out.close();
						
					}
					else if(work_type.equals("NEW_USER")){ //режим регистрации новой уч.записи
						final String login = in.readUTF();
						final String password = in.readUTF();
						final String port = in.readUTF();
						final String address = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress();
						boolean repeated = false;
						for(int i=0; i<users.size(); i++){
							if(login.equals(users.get(i).get_login())){
								repeated = true;
								break;
							}
						}
						if(repeated == false){
							users.add(new UserClass(login, password));
							message = "created";
							writeDatabase();
						}
						else{
							message = "not_created";
						}
						final Socket socket_out = new Socket(address, Integer.parseInt(port));
						final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						out.writeUTF(work_type);
						out.writeUTF(message);
						socket_out.close();
					}
					else if(work_type.equals("TAKE_USERS_ONLINE")){ //режим ответа на запрос об пользователях онлайн
						final String name = in.readUTF();
						final int userNumber = SearchUser(name);
						LinkedList<String> onlineUsers = new LinkedList<String>();
						for(int i=0; i<users.size(); i++){
							if(users.get(i).get_online() == true){
								onlineUsers.add(users.get(i).get_login());
							}
						}
						
						final Socket socket_out = new Socket(users.get(userNumber).get_ip(), users.get(userNumber).get_port());
						final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
						Integer Size = onlineUsers.size();
						out.writeUTF(work_type);
						out.writeUTF(Size.toString());
						for(String user_name:onlineUsers){
							out.writeUTF(user_name);							
						}
						socket_out.close();
					}
					else if(work_type.equals("USER_IS_ONLINE")){//режим оповещения кллиента онлайн
						final String name = in.readUTF();
						final int userNumber = SearchUser(name);
						users.get(userNumber).set_online(true);
						//без ответа
					}
					else if(work_type.equals("DIALOG")){
						final String UserName = in.readUTF();
						final int userNumber = SearchUser(UserName);
						final String FriendName = in.readUTF();
						final int friendNumber = SearchUser(FriendName);
						if(users.get(friendNumber).get_online() == true){
							message = in.readUTF();
							final Socket socket_out = new Socket(users.get(friendNumber).get_ip(), users.get(friendNumber).get_port());
							final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
							out.writeUTF(work_type);
							out.writeUTF(UserName);
							out.writeUTF(message);
							socket_out.close();
						}
						else{
							message = "Пользователь недоступен";
							final Socket socket_out = new Socket(users.get(userNumber).get_ip(), users.get(userNumber).get_port());
							final DataOutputStream out = new DataOutputStream(socket_out.getOutputStream());
							out.writeUTF(work_type);
							out.writeUTF(UserName);
							out.writeUTF(message);
							socket_out.close();
						}
					}
				}
				catch (UnknownHostException e){
					e.printStackTrace();
					System.out.println("Была ошибка. Работа продолжена.");
				} 
				catch (IOException e){
					e.printStackTrace();
					System.out.println("Была ошибка. Работа продолжена.");
				}
				finally{
					socket.close();
				}
			}
		}
		finally{
			serverSocket.close();
		}

	}

}
