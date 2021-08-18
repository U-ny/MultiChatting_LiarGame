package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ClientMain {

	Socket LoginSocket;
	Socket ChatSocket;
	boolean bool;
	String message;

	String nickname;
	public boolean getBoolean() {
		return bool;
	}

	public void startLiarGame() {
		// �������� thread �ʿ� ���⿡ runnable ��� thread ��ä ���
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("���ӽ���");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// ���� ��
						System.out.println("[���� ���� ����]");
						Platform.exit();// ���α׷� ����
					}
				}
			}
		};
		thread.start();
	}

	public void endLiarGame() {
		// �������� thread �ʿ� ���⿡ runnable ��� thread ��ä ���
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("��������");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// ���� ��
						System.out.println("[���� ���� ����]");
						Platform.exit();// ���α׷� ����
					}
				}
			}
		};
		thread.start();
	}

	public void startClient(String userName, String IP, int port) {
		// �������� thread �ʿ� ���⿡ runnable ��� thread ��ä ���

		try {
			System.out.println("startClient ����");
			LoginSocket = new Socket(IP, port);// ���� ���� ���� �� üũ
			ChatSocket = new Socket(IP, port + 1);
			System.out.println("���� ���� �Ϸ�");
			sendLogin(userName);
			System.out.println("sendLogin �Ϸ�");
			boolean chk = receiveLogin();// ���� �����̸� true
			System.out.println("chk�� ����" + chk);
			if (chk == true) {
				bool = true;
				nickname=userName;
			} else
				bool = false;
		} catch (Exception e) {
			if (!LoginSocket.isClosed()) {
				bool = false;
				stopClient();// ���� ��
				System.out.println("[���� ���� ����]");
				Platform.exit();// ���α׷� ����
			}
			if (!ChatSocket.isClosed()) {
				bool = false;
				stopClient();// ���� ��
				System.out.println("[���� ���� ����]");
				Platform.exit();// ���α׷� ����
			}
		}
	}

	// Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ�
	public void stopClient() {
		try {
			if (LoginSocket != null && !LoginSocket.isClosed()) {
				LoginSocket.close();
			}
			if (ChatSocket != null && !ChatSocket.isClosed()) {
				ChatSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �����κ��� �޼����� ���� �޴� �޼ҵ�
	public boolean receiveLogin() {
		while (true) {
			// ��� ���� ����
			try {
				InputStream in = LoginSocket.getInputStream();// �����κ��� ���� ����
				byte[] buffer = new byte[512];
				int length = in.read(buffer);// read �Լ��� ���� �Է� �޴´�.
				if (length == -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				System.out.println("���������մϴ�!");
				if (message.equals("��������")) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				stopClient();
				return false;
			}
		}
	}

	// ������ �޼����� �����ϴ� �޼ҵ�
	public void sendLogin(String message) {
		// ������ �����ϱ� ���ؼ��� thread �ʿ�, receive thread�� �ٸ�
		Thread thread = new Thread() {
			public void run() {
				
				try {
					OutputStream out = LoginSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	// �����κ��� �޼����� ���� �޴� �޼ҵ�
	/*public void receiveChat(TextArea textArea) {
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					// ��� ���� ����
					try {
						InputStream in = ChatSocket.getInputStream();// �����κ��� ���� ����
						byte[] buffer = new byte[512];
						int length = in.read(buffer);// read �Լ��� ���� �Է� �޴´�.
						if (length == -1)
							throw new IOException();
						String message = new String(buffer, 0, length, "UTF-8");
						System.out.println(message);
						Platform.runLater(() -> {
							textArea.appendText(message);
						});
					} catch (Exception e) {
						stopClient();
						break;
					}
				}

			}
		};
		thread.start();
	}*/

	// ������ �޼����� �����ϴ� �޼ҵ�
	public void sendChat(String message) {
		// ������ �����ϱ� ���ؼ��� thread �ʿ�, receive thread�� �ٸ�
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = ChatSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	public void requestNum() {
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("���ο���");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// ���� ��
						System.out.println("[���� ���� ����]");
						Platform.exit();// ���α׷� ����
					}
				}
			}
		};
		thread.start();
	}
}
