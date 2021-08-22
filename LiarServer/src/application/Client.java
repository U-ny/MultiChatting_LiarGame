package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Client {

	Socket LoginSocket;// 9876
	Socket ChatSocket;// 9877
	// Socket VoteSocket;// 9878

	String serverIP = "110.11.219.124";
	int loginPort = 9876;
	UserConnectInfo info = new UserConnectInfo(serverIP, loginPort);
	static int totalNum = 0;
	static Vector<String> names = new Vector<String>();
	static Vector<Integer> vec=new Vector<Integer>();
	String liarName;
	static int gameClient=0;
	
	
	public Client(Socket LoginSocket, Socket ChatSocket) {
		this.LoginSocket = LoginSocket;
		this.ChatSocket = ChatSocket;
		// this.VoteSocket = VoteSocket;
		receiveLogin();// �ݺ������� client�� ���� message ���� ���� �� �ֵ��� �����.
		receiveChat();
		// receiveVote();
	}

	// Ŭ���̾�Ʈ�κ��� �޼����� ���� �޴� �޼ҵ��Դϴ�.
	public void receiveLogin() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						InputStream in = LoginSocket.getInputStream();
						byte[] buffer = new byte[512];// ���� �̿� �ѹ��� 512����Ʈ ��ŭ
						int length = in.read(buffer);
						while (length == -1)
							throw new IOException();
						// �޼����� ���������� ���� ���

						// �޼����� ���������� ���� ���
						System.out.println("[�޼��� ���� ����]");
						String message = new String(buffer, 0, length, "UTF-8");
						// message�� ���� ���� nickName�� ������� ���̴�.
						boolean check = info.checkLogin(message);// �г��� �˻� �Լ�
						if (check == true) {// ���� ������ ���
							sendLogin("��������");
							MyDB.addUser(message, new UserConnectInfo(LoginSocket.getInetAddress().toString(),
									LoginSocket.getPort()));
							
							// ä�� �������� ���� ��������.
							++totalNum;// ������ ����Ǿ� �ִ� + �ִ� ������� ��
							names.add(message);// �̸� ��Ͽ� �߰�;
							vec.add(0);
							String nameMessage = "";
							for (String s : names) {
								nameMessage += s + " ";
							}
							String chatMessage = Integer.toString(totalNum) + "::" + message + "���� �����ϼ̽��ϴ�. "
									+ nameMessage;
							// 2::aa���� �����ϼ̽��ϴ�. aa bb
							for (Client client : Main.clients) {
								client.sendChat(chatMessage);
							}
							int numSizeNow=totalNum;
							
						} else {
							sendLogin("�����Ұ�");
						}
					}
				} catch (Exception e) {
					try {
						System.out.println("[�޼��� ���� ����] ���ú� �α��� ���� ");
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}

	// Ŭ���̾�Ʈ���� �޼����� �����ϴ� �޼ҵ�
	public void sendLogin(String message) {
		// Runnable library �̿��ؼ� thread ���� ���ְ�
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					// OutputStream ��� ����, �޼����� �����ְ��� �� ���� outputStream����
					OutputStream out = LoginSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[�޼��� �۽� ����]");
						// ���� �߻� �� ���� �Լ��� client�� ������ ��� clients�迭����
						// ���� �����ϴ� Client�� �����ش�.
						Main.clients.remove(Client.this);
						// ������ ���� client�� socket�� �ݴ´�.
						LoginSocket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		// Main threadPool�� �߰� �Ѵ�.
		Main.threadPool.submit(thread);
	}

	public void receiveChat() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						InputStream in = ChatSocket.getInputStream();
						byte[] buffer = new byte[512];// ���� �̿� �ѹ��� 512����Ʈ ��ŭ
						int length = in.read(buffer);
						while (length == -1)
							throw new IOException();
						System.out.println("[�޼��� ���� ����]");
						String message = new String(buffer, 0, length, "UTF-8");
						if (message.equals("���ӽ���")) {
							liarSelect();
						}
						else if (message.length()>=9 &&message.substring(0,8).equals(":�����Ұ̴ϴ�.")) {
							message=message.substring(8);
							MyDB.deleteUser(message);
							totalNum--;
							System.out.println("�������"+message);
							Main.clients.remove(Client.this);
							for(int i=0; i<names.size();i++) {
								System.out.println(names.get(i));
							}
							for(int i=0; i<names.size();i++) {
								if(names.get(i).equals(message)) {
									names.remove(i);
									vec.remove(i);
									break;
								}
							}
							for(int i=0; i<names.size();i++) {
								System.out.println(names.get(i));
							}
							String nameMessage="";
							for(String s:names)
							{
								nameMessage+=s+" ";
							}
							String chatMessage=Integer.toString(totalNum)+"::"+message+"���� �����ϼ̽��ϴ�. "+nameMessage;
							//2::aa���� �����ϼ̽��ϴ�. aa bb
							for(Client client: Main.clients) {
								client.sendChat(chatMessage);
							}
						} 
						else if(message.substring(0,3).equals("��ǥ:")){
							for(Client client: Main.clients) {
								client.sendChat("������ ��ǥ�߽��ϴ�. \n�ٽ� ��ǥ�� �Ұ����ϸ� ��ΰ� ��ǥ�ϸ� ����� �����˴ϴ�.");
							}
							String liar=message.substring(3);
							System.out.println(liar);
							System.out.println("���� ��Ͽ� �ִ� �����");
							for(int i=0; i<names.size();i++)
							{
								System.out.println(names.get(i));
							}
							System.out.println("===================");
							
							for(int i=0; i<names.size();i++)
							{
								if(names.get(i).equals(liar))
								{
									vec.set(i, vec.get(i)+1);
								}
							}
							gameClient--;
							System.out.println(gameClient);
							if(gameClient==0)
							{
								
								//���� ���� ���� ��� max�� ������, votedLiar�� �ε����� �� �ִ�. names �迭�� index�� ����
								//���� ���� ���� ����� index�� votedLiar, names���� ã���� �ȴ�.
								for(Client client: Main.clients) {
									client.sendChat("���̾�� "+liarName+"���̾����ϴ�.");
								}
							//vec �迭�� 0�� �ε��� � 1�� �ε��� � 2���ε��� � �޾Ҵ��� �� �� ����
							}
							
						}
						else {
							// ���� ���� �޼����� �ٸ� client���Ե� ���� �� �ֵ���
							for (Client client : Main.clients) {
								client.sendChat(message);
							}
						}

					}
				} catch (Exception e) {
					try {
						System.out.println("[�޼��� ���� ����] ");
						e.printStackTrace();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}

	// Ŭ���̾�Ʈ���� �޼����� �����ϴ� �޼ҵ�
	public void sendChat(String message) {
		// Runnable library �̿��ؼ� thread ���� ���ְ�
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					// OutputStream ��� ����, �޼����� �����ְ��� �� ���� outputStream����
					OutputStream out = ChatSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					// ���� �� �߻� �� �������� client�� �����ϱ� ���ؼ�
					// out���� write ���ش�.
					out.write(buffer);
					System.out.println("[�޼��� �۽� ����]");
					// ���������� ������� �����ߴٴ� ���� �˸��� ���� �ݵ�� flush ���־�� �Ѵ�.
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[�޼��� �۽� ����]");
						// ���� �߻� �� ���� �Լ��� client�� ������ ��� clients�迭����
						// ���� �����ϴ� Client�� �����ش�.
						Main.clients.remove(Client.this);
						// ������ ���� client�� socket�� �ݴ´�.
						ChatSocket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		// Main threadPool�� �߰� �Ѵ�.
		Main.threadPool.submit(thread);
	}

	public void liarSelect() {

		int cnt = names.size();// ���� ������ �ִ� client ��
		gameClient=cnt;
		Random rand = new Random();
		int liarIndex = rand.nextInt(cnt);// rand.nextInt()��ȯ �� 0~n�̸��� ����
		int clientCnt = 0;
		liarName=names.get(liarIndex);
		
		//���� �ؿ� �����ؾ� �Ѵ�.
		String word=wordProcess();
		Iterator<Client> iterator = Main.clients.iterator();// Iterator �̿� �ݺ�
		while (iterator.hasNext()) {
			// �ϳ��� ��� client �� �����Ѵ�.
			Client client = iterator.next();
			client.sendChat("====================================");
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			client.sendChat("������ �� ���۵˴ϴ�.\n");
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (liarName.equals(client.names.get(clientCnt))) {// liar �� ���
				client.sendChat("������ [���̾�]�Դϴ�. �ɸ��� �ʰ� �� �ൿ�ϼ���\n");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				client.sendChat("====================================");
				System.out.println("[���̾ �����Ǿ����ϴ�]");
			} else {
				client.sendChat("������ [�ù�]�Դϴ�.\n");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				client.sendChat("������� [" + word + "] �Դϴ�.\n");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				};

			}
			clientCnt++;
		}
	}

	public String wordProcess() {
		// ���� "����, ����, ����" �ܾ�� ���� ����
		ArrayList<String> topic = new ArrayList<>(); // ���ڿ� Ÿ���� ���� ��ü ����
		// add
		String giveTopic = "";
		topic.add("����");
		topic.add("����");
		topic.add("����");

		Collections.shuffle(topic); // shuffle�� ���� ���� ���� �������� ���� ���ġ
		String getTopic = topic.get(0); // mix�� ������ ù��° ���� ��������
		for (Client client : Main.clients) {

			client.sendChat("====================================");
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			client.sendChat("���� ������ " + getTopic + " �Դϴ�");
		}
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// ���� �� ������ ���þ����� �ҷ�����
		try {
			File fileName;
			if (getTopic == "����") {
				fileName = new File("E:\\JavaWork\\LiarServer\\Topic\\food.txt"); // Path = ������ �����϶� ���� ���þ�����
			} else if (getTopic == "����") {
				fileName = new File("E:\\JavaWork\\LiarServer\\Topic\\animal.txt"); // Path = ������ �����϶� ���� ���þ�����
			} else {// ����
				fileName = new File("E:\\JavaWork\\LiarServer\\Topic\\job.txt"); // Path = ������ �����϶� ���� ���þ�����
			}
			FileReader filereader = new FileReader(fileName);
			// �Է� ���� ����
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			ArrayList<String> getWord = new ArrayList<String>();
			while ((line = bufReader.readLine()) != null) {
				getWord.add(line);
			}
			// .readLine()�� ���� ���๮�ڸ� ���� �ʴ´�.

			// ���þ� �������� ����
			Collections.shuffle(getWord); // ����
			giveTopic = getWord.get(0); // ���� ���þ� ù��° �̱�

			bufReader.close();
			filereader.close();

		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			e.getStackTrace();
		}
		return giveTopic; // ���þ� ��ȯ
	}
}
