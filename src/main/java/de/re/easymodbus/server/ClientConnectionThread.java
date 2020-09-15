/*    */ package de.re.easymodbus.server;
/*    */ 
/*    */ import java.io.InputStream;
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
/*    */ class ClientConnectionThread
/*    */   extends Thread
/*    */ {
/*    */   private Socket socket;
/* 21 */   private byte[] inBuffer = new byte[1024];
/*    */   
/*    */   ModbusServer easyModbusTCPServer;
/*    */   
/*    */   public ClientConnectionThread(Socket socket, ModbusServer easyModbusTCPServer) {
/* 26 */     this.easyModbusTCPServer = easyModbusTCPServer;
/* 27 */     this.socket = socket;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 32 */     this.easyModbusTCPServer.setNumberOfConnectedClients(this.easyModbusTCPServer.getNumberOfConnectedClients() + 1);
/*    */ 
/*    */     
/*    */     try {
/* 36 */       this.socket.setSoTimeout(this.easyModbusTCPServer.getClientConnectionTimeout());
/*    */       
/* 38 */       InputStream inputStream = this.socket.getInputStream();
/* 39 */       while (((this.socket.isConnected()?1:0) & (this.socket.isClosed() ? 0 : 1) & (this.easyModbusTCPServer.getServerRunning()?1:0)) != 0) {
/*    */ 
/*    */         
/* 42 */         int numberOfBytes = inputStream.read(this.inBuffer);
/* 43 */         if (numberOfBytes > 4)
/* 44 */           (new ProcessReceivedDataThread(this.inBuffer, this.easyModbusTCPServer, this.socket)).start(); 
/* 45 */         Thread.sleep(5L);
/*    */       } 
/* 47 */       this.easyModbusTCPServer.setNumberOfConnectedClients(this.easyModbusTCPServer.getNumberOfConnectedClients() - 1);
/* 48 */       this.socket.close();
/* 49 */     } catch (Exception e) {
/*    */       
/* 51 */       this.easyModbusTCPServer.setNumberOfConnectedClients(this.easyModbusTCPServer.getNumberOfConnectedClients() - 1);
/*    */       
/*    */       try {
/* 54 */         this.socket.close();
/*    */       }
/* 56 */       catch (Exception exception) {}
/*    */       
/* 58 */       e.printStackTrace();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              d:\libs\EasyModbusJava.jar!\de\re\easymodbus\server\ClientConnectionThread.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */