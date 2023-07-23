package com.home.prec0723.network.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

//서버에 접속하는 각각의 클라이언트 마다 1:1대응하는 대화 전용 쓰레드 정의
public class MessageThread extends Thread {
	GUIServer guiServer;
	Socket socket;
	BufferedReader buffr; // 버퍼처리된 문자기반 입력스트림
	BufferedWriter buffw; // 버퍼처리된 문자기반 출력스트림
	
	boolean loopFlag=true; //쓰레드의 while문을 제어하는 flag

	public MessageThread(GUIServer guiServer, Socket socket) {
		this.guiServer = guiServer;
		this.socket = socket;

		try {
			buffr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			buffw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 듣기
	public void listen() {
		String msg = null;
		try {
			msg = buffr.readLine(); // 듣기

			//접속한 모든 사용자마다 대응되는 메세지쓰레드 객체의 sendMsg() 호출
			for (int i = 0; i < guiServer.vec.size(); i++) {
				MessageThread mt=guiServer.vec.get(i);
				mt.sendMsg(msg); // 다시보내기
			}
			// 로그 남기기
			guiServer.area.append(msg + "\n");

		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("나가서 못읽음");
			loopFlag=false;
			//명단에서 나 제거
			guiServer.vec.remove(this);
			guiServer.area.append("현재 접속자 수 "+guiServer.vec.size()+"\n");
		}
	}

	// 말하기
	public void sendMsg(String msg) {
		try {
			buffw.write(msg + "\n");
			buffw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 쓰레드는 run()의 닫는 브레이스를 만나면 소멸되므로, 죽이지 않으려면 무한루프로 처리해야함
	@Override
	public void run() {
		while (loopFlag) {
			listen(); // 끊임없이 듣고, 다시보냄
		}
	}
}
