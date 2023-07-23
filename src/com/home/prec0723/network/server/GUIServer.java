package com.home.prec0723.network.server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUIServer extends JFrame {
	JPanel p_north;
	JTextField t_port;
	JButton bt;

	JTextArea area;
	JScrollPane scroll;

	Thread runThread; // 메인 쓰레드를 대기상태에 빠지지 않게 하기 위함
	ServerSocket server; // 대화용이 아닌, 접속자 담지 및 대화용 소켓을 얻기 위한 서버소켓
	
	Vector<MessageThread> vec; //ArrayList와 동일, 단 Vector는 쓰레드 동기화를 지원함
	//즉, 다중 쓰레드 환경에서 공유되는 자원을 여러 쓰레드가 동시에 접근하려고 하는 것은 불안정함
	//이 때 벡터는 자체적으로 안전장치(Lock)가 마련되어있음
	

	public GUIServer() {
		p_north = new JPanel();
		t_port = new JTextField("7777", 10);
		bt = new JButton("서버가동");
		area = new JTextArea();
		scroll = new JScrollPane(area);
		vec=new Vector<MessageThread>();

		// 조립
		p_north.add(t_port);
		p_north.add(bt);

		add(p_north, BorderLayout.NORTH);
		add(scroll);

		setBounds(500, 200, 300, 400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runThread = new Thread() {
					@Override
					public void run() {
						runServer();
					}
				};
				runThread.start(); // runnable 진입

			}
		});
	}

	public void runServer() {
		// 끊임없이 다수의 클라이언트 접속 허용 및 처리
		int port = Integer.parseInt(t_port.getText()); // "7777"
		try {
			server = new ServerSocket(port); // 서버 생성
			area.append("서버 생성+\n");

			while (true) { //아래 코드를 무한루프문에 작성함으로써 다중 접속을 가능하게 함
				// 접속자가 감지되면, 대화용 소켓이 반환됨
				Socket socket = server.accept(); // 접속자가 있을 때까지 대기 상태에 빠짐
				String ip = socket.getInetAddress().getHostAddress();
				area.append(ip + "접속\n");
				
				//접속자가 발견되면 대화용 쓰레드를 생성하면서, 소켓을 넘겨줌
				MessageThread mt=new MessageThread(this, socket);
				mt.start(); //대화용 쓰레드 동작 시작
				
				//태어난 대화용 쓰레드를 벡터에 담아둠
				vec.add(mt);
				
				//현재까지 접속된 사용자 수 로그 출력
				area.append("현재까지 "+vec.size()+"명 접속 중\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GUIServer();
	}
}


//telnet cmd 접속방법 >> telnet ip port