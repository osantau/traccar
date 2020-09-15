/*    */ package de.re.easymodbus.server;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.net.ServerSocket;
/*    */ import java.net.Socket;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ class ListenerThread
/*    */   extends Thread
/*    */ {
/*    */   ModbusServer easyModbusTCPServer;
/*    */   
/*    */   public ListenerThread(ModbusServer easyModbusTCPServer) {
/* 24 */     this.easyModbusTCPServer = easyModbusTCPServer;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 29 */     ServerSocket serverSocket = null;
/*    */     try {
/* 31 */       serverSocket = new ServerSocket(this.easyModbusTCPServer.getPort());
/*    */ 
/*    */       
/* 34 */       while (((this.easyModbusTCPServer.getServerRunning()?1:0) & (isInterrupted() ? 0 : 1)) != 0) {
/*    */         
/* 36 */         Socket socket = serverSocket.accept();
/* 37 */         (new ClientConnectionThread(socket, this.easyModbusTCPServer)).start();
/*    */       } 
/* 39 */     } catch (IOException e) {
/* 40 */       System.out.println(e.getMessage());
/*    */       
/* 42 */       e.printStackTrace();
/*    */     } 
/*    */     
/* 45 */     if (serverSocket != null)
/*    */       try {
/* 47 */         serverSocket.close();
/* 48 */       } catch (IOException e) {
/*    */         
/* 50 */         e.printStackTrace();
/*    */       }  
/*    */   }
/*    */ }


/* Location:              d:\libs\EasyModbusJava.jar!\de\re\easymodbus\server\ListenerThread.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */