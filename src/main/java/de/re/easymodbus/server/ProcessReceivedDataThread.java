/*     */ package de.re.easymodbus.server;
/*     */ 
/*     */ import java.net.Socket;
/*     */ import java.util.Calendar;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class ProcessReceivedDataThread
/*     */   extends Thread
/*     */ {
/*     */   short[] inBuffer;
/*     */   ModbusServer easyModbusTCPServer;
/*     */   Socket socket;
/*     */   
/*     */   public ProcessReceivedDataThread(byte[] inBuffer, ModbusServer easyModbusTCPServer, Socket socket) {
/*  27 */     this.socket = socket;
/*  28 */     this.inBuffer = new short[inBuffer.length];
/*  29 */     for (int i = 0; i < inBuffer.length; i++)
/*     */     {
/*     */       
/*  32 */       this.inBuffer[i] = (short)((short)inBuffer[i] & 0xFF);
/*     */     }
/*  34 */     this.easyModbusTCPServer = easyModbusTCPServer;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*  40 */     synchronized (this.easyModbusTCPServer) {
/*     */       
/*  42 */       short[] wordData = new short[1];
/*  43 */       short[] byteData = new short[2];
/*  44 */       this.easyModbusTCPServer.receiveData = new ModbusProtocoll();
/*  45 */       this.easyModbusTCPServer.receiveData.timeStamp = Calendar.getInstance();
/*  46 */       this.easyModbusTCPServer.receiveData.request = true;
/*     */ 
/*     */       
/*  49 */       byteData[1] = this.inBuffer[0];
/*  50 */       byteData[0] = this.inBuffer[1];
/*  51 */       wordData[0] = (short)byteArrayToInt(byteData);
/*  52 */       this.easyModbusTCPServer.receiveData.transactionIdentifier = wordData[0];
/*     */ 
/*     */       
/*  55 */       byteData[1] = this.inBuffer[2];
/*  56 */       byteData[0] = this.inBuffer[3];
/*  57 */       wordData[0] = (short)byteArrayToInt(byteData);
/*  58 */       this.easyModbusTCPServer.receiveData.protocolIdentifier = wordData[0];
/*     */ 
/*     */       
/*  61 */       byteData[1] = this.inBuffer[4];
/*  62 */       byteData[0] = this.inBuffer[5];
/*  63 */       wordData[0] = (short)byteArrayToInt(byteData);
/*  64 */       this.easyModbusTCPServer.receiveData.length = wordData[0];
/*     */ 
/*     */       
/*  67 */       this.easyModbusTCPServer.receiveData.unitIdentifier = (byte)this.inBuffer[6];
/*     */ 
/*     */       
/*  70 */       this.easyModbusTCPServer.receiveData.functionCode = (byte)this.inBuffer[7];
/*     */ 
/*     */       
/*  73 */       byteData[1] = this.inBuffer[8];
/*  74 */       byteData[0] = this.inBuffer[9];
/*  75 */       wordData[0] = (short)byteArrayToInt(byteData);
/*  76 */       this.easyModbusTCPServer.receiveData.startingAdress = wordData[0];
/*     */       
/*  78 */       if (this.easyModbusTCPServer.receiveData.functionCode <= 4) {
/*     */ 
/*     */         
/*  81 */         byteData[1] = this.inBuffer[10];
/*  82 */         byteData[0] = this.inBuffer[11];
/*  83 */         wordData[0] = (short)byteArrayToInt(byteData);
/*  84 */         this.easyModbusTCPServer.receiveData.quantity = wordData[0];
/*     */       } 
/*  86 */       if (this.easyModbusTCPServer.receiveData.functionCode == 5) {
/*     */         
/*  88 */         this.easyModbusTCPServer.receiveData.receiveCoilValues = new short[1];
/*     */         
/*  90 */         byteData[0] = this.inBuffer[10];
/*  91 */         byteData[1] = this.inBuffer[11];
/*  92 */         this.easyModbusTCPServer.receiveData.receiveCoilValues[0] = (short)byteArrayToInt(byteData);
/*     */       } 
/*  94 */       if (this.easyModbusTCPServer.receiveData.functionCode == 6) {
/*     */         
/*  96 */         this.easyModbusTCPServer.receiveData.receiveRegisterValues = new int[1];
/*     */         
/*  98 */         byteData[1] = this.inBuffer[10];
/*  99 */         byteData[0] = this.inBuffer[11];
/* 100 */         this.easyModbusTCPServer.receiveData.receiveRegisterValues[0] = byteArrayToInt(byteData);
/*     */       } 
/* 102 */       if (this.easyModbusTCPServer.receiveData.functionCode == 15) {
/*     */ 
/*     */         
/* 105 */         byteData[1] = this.inBuffer[10];
/* 106 */         byteData[0] = this.inBuffer[11];
/* 107 */         wordData[0] = (short)byteArrayToInt(byteData);
/* 108 */         this.easyModbusTCPServer.receiveData.quantity = wordData[0];
/*     */         
/* 110 */         this.easyModbusTCPServer.receiveData.byteCount = (byte)this.inBuffer[12];
/*     */         
/* 112 */         if (this.easyModbusTCPServer.receiveData.byteCount % 2 != 0) {
/* 113 */           this.easyModbusTCPServer.receiveData.receiveCoilValues = new short[this.easyModbusTCPServer.receiveData.byteCount / 2 + 1];
/*     */         } else {
/* 115 */           this.easyModbusTCPServer.receiveData.receiveCoilValues = new short[this.easyModbusTCPServer.receiveData.byteCount / 2];
/*     */         } 
/* 117 */         for (int i = 0; i < this.easyModbusTCPServer.receiveData.byteCount; i++) {
/*     */           
/* 119 */           if (i % 2 == 1) {
/* 120 */             this.easyModbusTCPServer.receiveData.receiveCoilValues[i / 2] = (short)(this.easyModbusTCPServer.receiveData.receiveCoilValues[i / 2] + 256 * this.inBuffer[13 + i]);
/*     */           } else {
/* 122 */             this.easyModbusTCPServer.receiveData.receiveCoilValues[i / 2] = this.inBuffer[13 + i];
/*     */           } 
/*     */         } 
/* 125 */       }  if (this.easyModbusTCPServer.receiveData.functionCode == 16) {
/*     */ 
/*     */         
/* 128 */         byteData[1] = this.inBuffer[10];
/* 129 */         byteData[0] = this.inBuffer[11];
/* 130 */         wordData[0] = (short)byteArrayToInt(byteData);
/* 131 */         this.easyModbusTCPServer.receiveData.quantity = wordData[0];
/*     */         
/* 133 */         this.easyModbusTCPServer.receiveData.byteCount = (byte)this.inBuffer[12];
/* 134 */         this.easyModbusTCPServer.receiveData.receiveRegisterValues = new int[this.easyModbusTCPServer.receiveData.quantity];
/* 135 */         for (int i = 0; i < this.easyModbusTCPServer.receiveData.quantity; i++) {
/*     */ 
/*     */           
/* 138 */           byteData[1] = this.inBuffer[13 + i * 2];
/* 139 */           byteData[0] = this.inBuffer[14 + i * 2];
/* 140 */           this.easyModbusTCPServer.receiveData.receiveRegisterValues[i] = byteData[0];
/* 141 */           this.easyModbusTCPServer.receiveData.receiveRegisterValues[i] = this.easyModbusTCPServer.receiveData.receiveRegisterValues[i] + (byteData[1] << 8);
/*     */         } 
/*     */       } 
/* 144 */       this.easyModbusTCPServer.CreateAnswer(this.socket);
/* 145 */       this.easyModbusTCPServer.CreateLogData();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public int byteArrayToInt(short[] byteArray) {
/* 152 */     int returnValue = byteArray[0];
/* 153 */     returnValue += 256 * byteArray[1];
/* 154 */     return returnValue;
/*     */   }
/*     */ }


/* Location:              d:\libs\EasyModbusJava.jar!\de\re\easymodbus\server\ProcessReceivedDataThread.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */