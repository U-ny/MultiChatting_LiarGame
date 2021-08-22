package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ChatroomController implements Initializable {

	@FXML
	private Button exit;
	@FXML
	private Label userCount;
	@FXML
	private TextField userInput;
	@FXML
	private Button sendMessage;
	@FXML
	private TextArea chatArea;
	@FXML
	private ListView<String> listview;
	@FXML
	private Button game;
	@FXML
	private Label minute;
	@FXML
	private Label second;
	@FXML
	private Button startBtn;
	@FXML
	private Button rulePop;
	@FXML
	public Label myLabel;
	@FXML
	public ComboBox<String> combobox;
	@FXML
	public Button voteStart;
	
	
	ObservableList<String> members = FXCollections.observableArrayList();
	ClientMain clientSource;
	String nickname;
	CountThread t;

	@FXML
	void start() {
		if (startBtn.getText().equals("����")) {
			t = new CountThread(minute, second, 60);
			t.start();
			Platform.runLater(() -> {
				startBtn.setText("����");// ��ư�� ���� ���� �ٲ��ֱ�
			});
		} else {
			t.stopThread();
			Platform.runLater(() -> {
				startBtn.setText("����");// ��ư�� ���� ���� �ٲ��ֱ�
			});
		}
	}

	@FXML
	void gameStart() {
		if (game.getText().equals("���ӽ���")) {
			t = new CountThread(minute, second, 60);
			t.start();
			clientSource.startLiarGame();
			Platform.runLater(() -> {
				game.setText("��������");// ��ư�� ���� ���� �ٲ��ֱ�
			});
			Platform.runLater(() -> {
				voteStart.setDisable(false);
			});
			Platform.runLater(() -> {
				game.setDisable(true);
			});
		} else {
			clientSource.endLiarGame();
			t.stopThread();
			Platform.runLater(() -> {
				game.setText("���ӽ���");// ��ư�� ���� ���� �ٲ��ֱ�
			});
		}
	}

	@FXML
	public void logout() {
		String message = ":�����Ұ̴ϴ�." + clientSource.nickname;
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = clientSource.ChatSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					clientSource.stopClient();
				}
			}
		};
		thread.start();
	}

	public ChatroomController() {
		System.out.println("Controller Constuctor called");
		// clientSource = new ClientMain();
	}

	@FXML
	public void Send() {
		String message = clientSource.nickname + ": " + userInput.getText();
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = clientSource.ChatSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					clientSource.stopClient();
				}
			}
		};
		thread.start();
		userInput.clear(); // ���� ����
	}

	/*
	 * @FXML public void sendMessage() {
	 * //clientSource.sendChat(userInput.getText()); userInput.clear(); // ���� ���� }
	 */

	public void closeStage() {
		Stage stage11 = (Stage) exit.getScene().getWindow();
		Platform.runLater(() -> {
			stage11.close();
		});
		// clientSource.receiveChat(chatArea);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// userCount.setText();
		clientSource = new ClientMain();
		listview.setItems(FXCollections.observableArrayList());
		exit.setOnAction(e -> {
			logout();
			exitPressed(e);
		});
		voteStart.setOnAction(e -> {
			clientSource.sendChat("��ǥ����");
			voteConfirm();
		});
		rulePop.setOnAction(e->{
			rulePopPage();
		});
		// sendMessage.setOnAction(e->{
		// Send();
		// });

		userInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if (arg0.getCode() == KeyCode.ENTER) {
					Send();
				}
			}

		});
	}

	String message;
	String vectorName;
	String vec = "";

	public void receive() {
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					// ��� ���� ����
					try {
						InputStream in = clientSource.ChatSocket.getInputStream();// �����κ��� ���� ����
						byte[] buffer = new byte[512];
						int length = in.read(buffer);// read �Լ��� ���� �Է� �޴´�.
						if (length == -1)
							throw new IOException();
						message = new String(buffer, 0, length, "UTF-8");
						if(message.equals("��ǥ����"))
						{
							if(voteStart.getText().equals("��ǥ����"))
							{
								voteConfirm();
							}
						}
						else if (message.charAt(1) == ':' && message.charAt(2) == ':')// ���� ���� ���� ��
						{
							char mess = message.charAt(0);
							System.out.println(mess);
							Platform.runLater(() -> {
								userCount.setText(Character.toString(mess));// �̰� �ϳ��� �Ǵ���?
							});
							String a = message.substring(3);
							System.out.println(a);
							vectorName = "";
							int c = 0;
							for (int i = 0; i < a.length(); i++) {
								if (a.charAt(i) == '.') {
									c = i;
									vectorName = a.substring(i + 1);
									break;
								}
							}
							message = a.substring(0, c + 1);
							System.out.println(message);
							Platform.runLater(() -> {
								chatArea.appendText(message + "\n");
							});
							vec = "";// �ʱ�ȭ
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									listview.getItems().clear();
								}
							});
							// listview.getItems().clear();

							ArrayList<String> arr = new ArrayList<String>();
							for (int i = 0; i < vectorName.length() - 1; i++) {
								if (vectorName.charAt(i) == ' ')
									continue;
								if (vectorName.charAt(i) != ' ' && vectorName.charAt(i + 1) != ' ') {
									vec += vectorName.charAt(i);
								} else if (vectorName.charAt(i) != ' ' && vectorName.charAt(i + 1) == ' ') {
									vec += vectorName.charAt(i);
									System.out.println(vec);
									arr.add(vec);
									// listview.getItems().addAll(ArrayList<String> arr);
									// listview.getItems().add(vec);
									/*
									 * Platform.runLater(new Runnable() {
									 * 
									 * @Override public void run() { listview.getItems().add(vec); } });
									 */
									vec = "";
								}
							}
							members = FXCollections.observableArrayList(arr);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									listview.getItems().addAll(arr);
								}
							});
						} else if (message.equals("====================================")) {
							t = new CountThread(minute, second, 60);
							t.start();
							startBtn.setDisable(true);
							Platform.runLater(() -> {
								voteStart.setDisable(false);
							});
							Platform.runLater(() -> {
								game.setText("��������");// ��ư�� ���� ���� �ٲ��ֱ�
							});
							Platform.runLater(() -> {
								game.setDisable(true);
							});

						} else if (message.equals("��������")) {
							try {
								TimeUnit.SECONDS.sleep(1);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							startBtn.setDisable(false);
							Platform.runLater(() -> {
								chatArea.appendText(message + "\n");
								game.setText("���ӽ���");// ��ư�� ���� ���� �ٲ��ֱ�
							});
						} else {
							Platform.runLater(() -> {
								chatArea.appendText(message + "\n");
							});
						}

					} catch (Exception e) {
						clientSource.stopClient();
						break;
					}
				}

			}
		};
		thread.start();
	}
	public void rulePopPage() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("rule.fxml"));
		Parent root;
		try {
			root = (Parent) loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("rule.css").toString());
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void voteConfirm() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("vote.fxml"));
		Parent root;
		try {
			root = (Parent) loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("vote.css").toString());
			VoteController pop = loader.getController();
			pop.initFunc(members,clientSource);

			Stage stage = new Stage();
			
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Platform.runLater(() -> {
			voteStart.setDisable(true);// ��ư�� ���� ���� �ٲ��ֱ�
		});
		Platform.runLater(() -> {
			voteStart.setText("��ǥ�Ϸ�");// ��ư�� ���� ���� �ٲ��ֱ�
		});
	}

	private void exitPressed(ActionEvent event) {
		closeStage();
		// clientSource.stopClient();
	}

	public void initData(ClientMain loginSource, String name) {
		clientSource = loginSource;
		nickname = name;
		receive();
	}
}
