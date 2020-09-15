/*      */ package de.re.easymodbus.server;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.net.Socket;
/*      */ import java.util.Calendar;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class ModbusServer
/*      */   extends Thread
/*      */ {
/*   24 */   private int port = 502;
/*      */   protected ModbusProtocoll receiveData;
/*   26 */   protected ModbusProtocoll sendData = new ModbusProtocoll();
/*   27 */   public int[] holdingRegisters = new int[65535];
/*   28 */   public int[] inputRegisters = new int[65535];
/*   29 */   public boolean[] coils = new boolean[65535];
/*   30 */   public boolean[] discreteInputs = new boolean[65535];
/*   31 */   private int numberOfConnections = 0;
/*      */   public boolean udpFlag;
/*   33 */   private int clientConnectionTimeout = 10000;
/*      */   
/*   35 */   private ModbusProtocoll[] modbusLogData = new ModbusProtocoll[100];
/*      */   
/*      */   private boolean functionCode1Disabled;
/*      */   
/*      */   private boolean functionCode2Disabled;
/*      */   
/*      */   private boolean functionCode3Disabled;
/*      */   
/*      */   private boolean functionCode4Disabled;
/*      */   
/*      */   private boolean functionCode5Disabled;
/*      */   
/*      */   private boolean functionCode6Disabled;
/*      */   private boolean functionCode15Disabled;
/*      */   private boolean functionCode16Disabled;
/*      */   private boolean serverRunning;
/*      */   private ListenerThread listenerThread;
/*      */   protected ICoilsChangedDelegator notifyCoilsChanged;
/*      */   protected IHoldingRegistersChangedDelegator notifyHoldingRegistersChanged;
/*      */   protected INumberOfConnectedClientsChangedDelegator notifyNumberOfConnectedClientsChanged;
/*      */   protected ILogDataChangedDelegator notifyLogDataChanged;
/*      */   
/*      */   public ModbusServer() {
/*   58 */     System.out.println("EasyModbus Server Library");
/*   59 */     System.out.println("Copyright (c) Stefan Rossmann Engineering Solutions");
/*   60 */     System.out.println("www.rossmann-engineering.de");
/*   61 */     System.out.println("");
/*   62 */     System.out.println("Creative commons license");
/*   63 */     System.out.println("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)");
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   protected void finalize() {
/*   69 */     this.serverRunning = false;
/*   70 */     this.listenerThread.stop();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void Listen() throws IOException {
/*   93 */     this.serverRunning = true;
/*   94 */     this.listenerThread = new ListenerThread(this);
/*   95 */     this.listenerThread.start();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void StopListening() {
/*  104 */     this.serverRunning = false;
/*  105 */     this.listenerThread.stop();
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   protected void CreateAnswer(Socket socket) {
/*  111 */     switch (this.receiveData.functionCode) {
/*      */ 
/*      */       
/*      */       case 1:
/*  115 */         if (!this.functionCode1Disabled) {
/*  116 */           ReadCoils(socket);
/*      */           break;
/*      */         } 
/*  119 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  120 */         this.sendData.exceptionCode = 1;
/*  121 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */       
/*      */       case 2:
/*  126 */         if (!this.functionCode2Disabled) {
/*  127 */           ReadDiscreteInputs(socket);
/*      */           break;
/*      */         } 
/*  130 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  131 */         this.sendData.exceptionCode = 1;
/*  132 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case 3:
/*  138 */         if (!this.functionCode3Disabled) {
/*  139 */           ReadHoldingRegisters(socket);
/*      */           break;
/*      */         } 
/*  142 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  143 */         this.sendData.exceptionCode = 1;
/*  144 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case 4:
/*  150 */         if (!this.functionCode4Disabled) {
/*  151 */           ReadInputRegisters(socket);
/*      */           break;
/*      */         } 
/*  154 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  155 */         this.sendData.exceptionCode = 1;
/*  156 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case 5:
/*  162 */         if (!this.functionCode5Disabled) {
/*  163 */           WriteSingleCoil(socket);
/*      */           break;
/*      */         } 
/*  166 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  167 */         this.sendData.exceptionCode = 1;
/*  168 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case 6:
/*  174 */         if (!this.functionCode6Disabled) {
/*  175 */           WriteSingleRegister(socket);
/*      */           break;
/*      */         } 
/*  178 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  179 */         this.sendData.exceptionCode = 1;
/*  180 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case 15:
/*  186 */         if (!this.functionCode15Disabled) {
/*  187 */           WriteMultipleCoils(socket);
/*      */           break;
/*      */         } 
/*  190 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  191 */         this.sendData.exceptionCode = 1;
/*  192 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */ 
/*      */       
/*      */       case 16:
/*  198 */         if (!this.functionCode16Disabled) {
/*  199 */           WriteMultipleRegisters(socket);
/*      */           break;
/*      */         } 
/*  202 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 144);
/*  203 */         this.sendData.exceptionCode = 1;
/*  204 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */ 
/*      */       
/*      */       default:
/*  209 */         this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  210 */         this.sendData.exceptionCode = 1;
/*  211 */         sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
/*      */         break;
/*      */     } 
/*      */     
/*  215 */     this.sendData.timeStamp = Calendar.getInstance();
/*      */   }
/*      */   
/*      */   private void ReadCoils(Socket socket) {
/*      */     byte[] data;
/*  220 */     this.sendData = new ModbusProtocoll();
/*  221 */     this.sendData.response = true;
/*      */     
/*  223 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  224 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  226 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  227 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  228 */     if ((((this.receiveData.quantity < 1) ? 1 : 0) | ((this.receiveData.quantity > 2000) ? 1 : 0)) != 0) {
/*      */       
/*  230 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  231 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  233 */     if (this.receiveData.startingAdress + 1 + this.receiveData.quantity > 65535) {
/*      */       
/*  235 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  236 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  238 */     if (this.receiveData.quantity % 8 == 0) {
/*  239 */       this.sendData.byteCount = (byte)(this.receiveData.quantity / 8);
/*      */     } else {
/*  241 */       this.sendData.byteCount = (byte)(this.receiveData.quantity / 8 + 1);
/*      */     } 
/*  243 */     this.sendData.sendCoilValues = new boolean[this.receiveData.quantity];
/*      */     
/*  245 */     System.arraycopy(this.coils, this.receiveData.startingAdress + 1, this.sendData.sendCoilValues, 0, this.sendData.sendCoilValues.length);
/*      */ 
/*      */     
/*  248 */     if (this.sendData.exceptionCode > 0) {
/*  249 */       data = new byte[9];
/*      */     } else {
/*  251 */       data = new byte[9 + this.sendData.byteCount];
/*  252 */     }  byte[] byteData = new byte[2];
/*      */     
/*  254 */     this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  257 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  258 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  261 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  262 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  265 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  266 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  269 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  272 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */     
/*  275 */     data[8] = (byte)(this.sendData.byteCount & 0xFF);
/*      */     
/*  277 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  279 */       data[7] = this.sendData.errorCode;
/*  280 */       data[8] = this.sendData.exceptionCode;
/*  281 */       this.sendData.sendCoilValues = null;
/*      */     } 
/*      */     
/*  284 */     if (this.sendData.sendCoilValues != null) {
/*  285 */       for (int i = 0; i < this.sendData.byteCount; i++) {
/*      */         
/*  287 */         byteData = new byte[2];
/*  288 */         for (int j = 0; j < 8; j++) {
/*      */           byte boolValue;
/*      */ 
/*      */           
/*  292 */           if (this.sendData.sendCoilValues[i * 8 + j]) {
/*  293 */             boolValue = 1;
/*      */           } else {
/*  295 */             boolValue = 0;
/*  296 */           }  byteData[1] = (byte)(byteData[1] | boolValue << j);
/*  297 */           if (i * 8 + j + 1 >= this.sendData.sendCoilValues.length)
/*      */             break; 
/*      */         } 
/*  300 */         data[9 + i] = byteData[1];
/*      */       } 
/*      */     }
/*  303 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  305 */         OutputStream outputStream = socket.getOutputStream();
/*  306 */         outputStream.write(data);
/*  307 */       } catch (IOException e) {
/*      */         
/*  309 */         e.printStackTrace();
/*      */       }  
/*      */   }
/*      */   
/*      */   private void ReadDiscreteInputs(Socket socket) {
/*      */     byte[] data;
/*  315 */     this.sendData = new ModbusProtocoll();
/*  316 */     this.sendData.response = true;
/*      */     
/*  318 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  319 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  321 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  322 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  323 */     if ((((this.receiveData.quantity < 1) ? 1 : 0) | ((this.receiveData.quantity > 2000) ? 1 : 0)) != 0) {
/*      */       
/*  325 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  326 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  328 */     if (this.receiveData.startingAdress + 1 + this.receiveData.quantity > 65535) {
/*      */       
/*  330 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  331 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  333 */     if (this.receiveData.quantity % 8 == 0) {
/*  334 */       this.sendData.byteCount = (byte)(this.receiveData.quantity / 8);
/*      */     } else {
/*  336 */       this.sendData.byteCount = (byte)(this.receiveData.quantity / 8 + 1);
/*      */     } 
/*  338 */     this.sendData.sendCoilValues = new boolean[this.receiveData.quantity];
/*  339 */     System.arraycopy(this.discreteInputs, this.receiveData.startingAdress + 1, this.sendData.sendCoilValues, 0, this.receiveData.quantity);
/*      */ 
/*      */ 
/*      */     
/*  343 */     if (this.sendData.exceptionCode > 0) {
/*  344 */       data = new byte[9];
/*      */     } else {
/*  346 */       data = new byte[9 + this.sendData.byteCount];
/*  347 */     }  byte[] byteData = new byte[2];
/*  348 */     this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  351 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  352 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  355 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  356 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  359 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  360 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  363 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  366 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */     
/*  369 */     data[8] = (byte)(this.sendData.byteCount & 0xFF);
/*      */ 
/*      */     
/*  372 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  374 */       data[7] = this.sendData.errorCode;
/*  375 */       data[8] = this.sendData.exceptionCode;
/*  376 */       this.sendData.sendCoilValues = null;
/*      */     } 
/*      */     
/*  379 */     if (this.sendData.sendCoilValues != null) {
/*  380 */       for (int i = 0; i < this.sendData.byteCount; i++) {
/*      */         
/*  382 */         byteData = new byte[2];
/*  383 */         for (int j = 0; j < 8; j++) {
/*      */           byte boolValue;
/*      */ 
/*      */           
/*  387 */           if (this.sendData.sendCoilValues[i * 8 + j]) {
/*  388 */             boolValue = 1;
/*      */           } else {
/*  390 */             boolValue = 0;
/*  391 */           }  byteData[1] = (byte)(byteData[1] | boolValue << j);
/*  392 */           if (i * 8 + j + 1 >= this.sendData.sendCoilValues.length)
/*      */             break; 
/*      */         } 
/*  395 */         data[9 + i] = byteData[1];
/*      */       } 
/*      */     }
/*  398 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0) {
/*      */       try {
/*  400 */         OutputStream outputStream = socket.getOutputStream();
/*  401 */         outputStream.write(data);
/*  402 */       } catch (IOException e) {
/*      */         
/*  404 */         e.printStackTrace();
/*      */       } 
/*      */     }
/*      */   }
/*      */   
/*      */   private void ReadHoldingRegisters(Socket socket) {
/*      */     byte[] data;
/*  411 */     this.sendData = new ModbusProtocoll();
/*  412 */     this.sendData.response = true;
/*      */     
/*  414 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  415 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  417 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  418 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  419 */     if ((((this.receiveData.quantity < 1) ? 1 : 0) | ((this.receiveData.quantity > 125) ? 1 : 0)) != 0) {
/*      */       
/*  421 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  422 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  424 */     if (this.receiveData.startingAdress + 1 + this.receiveData.quantity > 65535) {
/*      */       
/*  426 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  427 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  429 */     this.sendData.byteCount = 
/*  430 */       (short)(2 * this.receiveData.quantity);
/*  431 */     this.sendData.sendRegisterValues = new int[this.receiveData.quantity];
/*  432 */     System.arraycopy(this.holdingRegisters, this.receiveData.startingAdress + 1, this.sendData.sendRegisterValues, 0, this.receiveData.quantity);
/*      */     
/*  434 */     if (this.sendData.exceptionCode > 0) {
/*  435 */       this.sendData.length = 3;
/*      */     } else {
/*  437 */       this.sendData.length = (short)(3 + this.sendData.byteCount);
/*      */     } 
/*      */ 
/*      */     
/*  441 */     if (this.sendData.exceptionCode > 0) {
/*  442 */       data = new byte[9];
/*      */     } else {
/*  444 */       data = new byte[9 + this.sendData.byteCount];
/*  445 */     }  this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  448 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  449 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  452 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  453 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  456 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  457 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  460 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  463 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */     
/*  466 */     data[8] = (byte)(this.sendData.byteCount & 0xFF);
/*      */     
/*  468 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  470 */       data[7] = this.sendData.errorCode;
/*  471 */       data[8] = this.sendData.exceptionCode;
/*  472 */       this.sendData.sendRegisterValues = null;
/*      */     } 
/*      */ 
/*      */     
/*  476 */     if (this.sendData.sendRegisterValues != null) {
/*  477 */       for (int i = 0; i < this.sendData.byteCount / 2; i++) {
/*      */         
/*  479 */         data[9 + i * 2] = (byte)((this.sendData.sendRegisterValues[i] & 0xFF00) >> 8);
/*  480 */         data[10 + i * 2] = (byte)(this.sendData.sendRegisterValues[i] & 0xFF);
/*      */       } 
/*      */     }
/*  483 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  485 */         OutputStream outputStream = socket.getOutputStream();
/*  486 */         outputStream.write(data);
/*  487 */       } catch (IOException e) {
/*      */         
/*  489 */         e.printStackTrace();
/*      */       }  
/*      */   }
/*      */   
/*      */   private void ReadInputRegisters(Socket socket) {
/*      */     byte[] data;
/*  495 */     this.sendData = new ModbusProtocoll();
/*  496 */     this.sendData.response = true;
/*      */     
/*  498 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  499 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  501 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  502 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  503 */     if ((((this.receiveData.quantity < 1) ? 1 : 0) | ((this.receiveData.quantity > 125) ? 1 : 0)) != 0) {
/*      */       
/*  505 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  506 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  508 */     if (this.receiveData.startingAdress + 1 + this.receiveData.quantity > 65535) {
/*      */       
/*  510 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  511 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  513 */     this.sendData.byteCount = (short)(2 * this.receiveData.quantity);
/*  514 */     this.sendData.sendRegisterValues = new int[this.receiveData.quantity];
/*  515 */     System.arraycopy(this.inputRegisters, this.receiveData.startingAdress + 1, this.sendData.sendRegisterValues, 0, this.receiveData.quantity);
/*      */     
/*  517 */     if (this.sendData.exceptionCode > 0) {
/*  518 */       this.sendData.length = 3;
/*      */     } else {
/*  520 */       this.sendData.length = (short)(3 + this.sendData.byteCount);
/*      */     } 
/*      */     
/*  523 */     if (this.sendData.exceptionCode > 0) {
/*  524 */       data = new byte[9];
/*      */     } else {
/*  526 */       data = new byte[9 + this.sendData.byteCount];
/*  527 */     }  this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  530 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  531 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  534 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  535 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  538 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  539 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  542 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  545 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */     
/*  548 */     data[8] = (byte)(this.sendData.byteCount & 0xFF);
/*      */ 
/*      */     
/*  551 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  553 */       data[7] = this.sendData.errorCode;
/*  554 */       data[8] = this.sendData.exceptionCode;
/*  555 */       this.sendData.sendRegisterValues = null;
/*      */     } 
/*      */ 
/*      */     
/*  559 */     if (this.sendData.sendRegisterValues != null) {
/*  560 */       for (int i = 0; i < this.sendData.byteCount / 2; i++) {
/*      */         
/*  562 */         data[9 + i * 2] = (byte)((this.sendData.sendRegisterValues[i] & 0xFF00) >> 8);
/*  563 */         data[10 + i * 2] = (byte)(this.sendData.sendRegisterValues[i] & 0xFF);
/*      */       } 
/*      */     }
/*  566 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  568 */         OutputStream outputStream = socket.getOutputStream();
/*  569 */         outputStream.write(data);
/*  570 */       } catch (IOException e) {
/*      */         
/*  572 */         e.printStackTrace();
/*      */       }  
/*      */   }
/*      */   
/*      */   private void WriteSingleCoil(Socket socket) {
/*      */     byte[] data;
/*  578 */     this.sendData = new ModbusProtocoll();
/*  579 */     this.sendData.response = true;
/*      */     
/*  581 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  582 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  584 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  585 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  586 */     this.sendData.startingAdress = this.receiveData.startingAdress;
/*  587 */     this.sendData.receiveCoilValues = this.receiveData.receiveCoilValues;
/*  588 */     if ((((this.receiveData.receiveCoilValues[0] != 0) ? 1 : 0) & ((this.receiveData.receiveCoilValues[0] != 255) ? 1 : 0)) != 0) {
/*      */       
/*  590 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  591 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  593 */     if (this.receiveData.startingAdress + 1 > 65535) {
/*      */       
/*  595 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  596 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  598 */     if (this.receiveData.receiveCoilValues[0] > 0)
/*      */     {
/*  600 */       this.coils[this.receiveData.startingAdress + 1] = true;
/*      */     }
/*  602 */     if (this.receiveData.receiveCoilValues[0] == 0)
/*      */     {
/*  604 */       this.coils[this.receiveData.startingAdress + 1] = false;
/*      */     }
/*  606 */     if (this.sendData.exceptionCode > 0) {
/*  607 */       this.sendData.length = 3;
/*      */     } else {
/*  609 */       this.sendData.length = 6;
/*      */     } 
/*      */     
/*  612 */     if (this.sendData.exceptionCode > 0) {
/*  613 */       data = new byte[9];
/*      */     } else {
/*  615 */       data = new byte[12];
/*      */     } 
/*  617 */     this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  620 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  621 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  624 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  625 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  628 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  629 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  632 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  635 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */ 
/*      */     
/*  639 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  641 */       data[7] = this.sendData.errorCode;
/*  642 */       data[8] = this.sendData.exceptionCode;
/*  643 */       this.sendData.sendRegisterValues = null;
/*      */     }
/*      */     else {
/*      */       
/*  647 */       data[8] = (byte)((this.receiveData.startingAdress & 0xFF00) >> 8);
/*  648 */       data[9] = (byte)(this.receiveData.startingAdress & 0xFF);
/*      */       
/*  650 */       data[10] = (byte)this.receiveData.receiveCoilValues[0];
/*  651 */       data[11] = 0;
/*      */     } 
/*      */ 
/*      */     
/*  655 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  657 */         OutputStream outputStream = socket.getOutputStream();
/*  658 */         outputStream.write(data);
/*  659 */       } catch (IOException e) {
/*      */         
/*  661 */         e.printStackTrace();
/*      */       }  
/*  663 */     if (this.notifyCoilsChanged != null)
/*  664 */       this.notifyCoilsChanged.coilsChangedEvent(); 
/*      */   }
/*      */   
/*      */   private void WriteSingleRegister(Socket socket) {
/*      */     byte[] data;
/*  669 */     this.sendData = new ModbusProtocoll();
/*  670 */     this.sendData.response = true;
/*      */     
/*  672 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  673 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  675 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  676 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  677 */     this.sendData.startingAdress = this.receiveData.startingAdress;
/*  678 */     this.sendData.receiveRegisterValues = this.receiveData.receiveRegisterValues;
/*      */     
/*  680 */     if ((((this.receiveData.receiveRegisterValues[0] < 0) ? 1 : 0) | ((this.receiveData.receiveRegisterValues[0] > 65535) ? 1 : 0)) != 0) {
/*      */       
/*  682 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  683 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  685 */     if (this.receiveData.startingAdress + 1 > 65535) {
/*      */       
/*  687 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  688 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  690 */     this.holdingRegisters[this.receiveData.startingAdress + 1] = this.receiveData.receiveRegisterValues[0];
/*  691 */     if (this.sendData.exceptionCode > 0) {
/*  692 */       this.sendData.length = 3;
/*      */     } else {
/*  694 */       this.sendData.length = 6;
/*      */     } 
/*      */     
/*  697 */     if (this.sendData.exceptionCode > 0) {
/*  698 */       data = new byte[9];
/*      */     } else {
/*  700 */       data = new byte[12];
/*      */     } 
/*  702 */     this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */ 
/*      */     
/*  706 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  707 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  710 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  711 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  714 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  715 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  718 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  721 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */ 
/*      */     
/*  725 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  727 */       data[7] = this.sendData.errorCode;
/*  728 */       data[8] = this.sendData.exceptionCode;
/*  729 */       this.sendData.sendRegisterValues = null;
/*      */     }
/*      */     else {
/*      */       
/*  733 */       data[8] = (byte)((this.receiveData.startingAdress & 0xFF00) >> 8);
/*  734 */       data[9] = (byte)(this.receiveData.startingAdress & 0xFF);
/*      */       
/*  736 */       data[10] = (byte)((this.receiveData.receiveRegisterValues[0] & 0xFF00) >> 8);
/*  737 */       data[11] = (byte)(this.receiveData.receiveRegisterValues[0] & 0xFF);
/*      */     } 
/*      */     
/*  740 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  742 */         OutputStream outputStream = socket.getOutputStream();
/*  743 */         outputStream.write(data);
/*  744 */       } catch (IOException e) {
/*      */         
/*  746 */         e.printStackTrace();
/*      */       }  
/*  748 */     if (this.notifyHoldingRegistersChanged != null)
/*  749 */       this.notifyHoldingRegistersChanged.holdingRegistersChangedEvent(); 
/*      */   }
/*      */   
/*      */   private void WriteMultipleCoils(Socket socket) {
/*      */     byte[] data;
/*  754 */     this.sendData = new ModbusProtocoll();
/*  755 */     this.sendData.response = true;
/*      */     
/*  757 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  758 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  760 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  761 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  762 */     this.sendData.startingAdress = this.receiveData.startingAdress;
/*  763 */     this.sendData.quantity = this.receiveData.quantity;
/*      */     
/*  765 */     if ((((this.receiveData.quantity == 0) ? 1 : 0) | ((this.receiveData.quantity > 1968) ? 1 : 0)) != 0) {
/*      */       
/*  767 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  768 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  770 */     if (this.receiveData.startingAdress + 1 + this.receiveData.quantity > 65535) {
/*      */       
/*  772 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
/*  773 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  775 */     for (int i = 0; i < this.receiveData.quantity; i++) {
/*      */       
/*  777 */       int shift = i % 16;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  787 */       int mask = 1;
/*  788 */       mask <<= shift;
/*  789 */       if ((this.receiveData.receiveCoilValues[i / 16] & mask) == 0) {
/*  790 */         this.coils[this.receiveData.startingAdress + i + 1] = false;
/*      */       } else {
/*  792 */         this.coils[this.receiveData.startingAdress + i + 1] = true;
/*      */       } 
/*      */     } 
/*  795 */     if (this.sendData.exceptionCode > 0) {
/*  796 */       this.sendData.length = 3;
/*      */     } else {
/*  798 */       this.sendData.length = 6;
/*      */     } 
/*      */     
/*  801 */     if (this.sendData.exceptionCode > 0) {
/*  802 */       data = new byte[9];
/*      */     } else {
/*  804 */       data = new byte[12];
/*      */     } 
/*  806 */     this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  809 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  810 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  813 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  814 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  817 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  818 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  821 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  824 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */ 
/*      */     
/*  828 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  830 */       data[7] = this.sendData.errorCode;
/*  831 */       data[8] = this.sendData.exceptionCode;
/*  832 */       this.sendData.sendRegisterValues = null;
/*      */     }
/*      */     else {
/*      */       
/*  836 */       data[8] = (byte)((this.receiveData.startingAdress & 0xFF00) >> 8);
/*  837 */       data[9] = (byte)(this.receiveData.startingAdress & 0xFF);
/*      */       
/*  839 */       data[10] = (byte)((this.receiveData.quantity & 0xFF00) >> 8);
/*  840 */       data[11] = (byte)(this.receiveData.quantity & 0xFF);
/*      */     } 
/*      */     
/*  843 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  845 */         OutputStream outputStream = socket.getOutputStream();
/*  846 */         outputStream.write(data);
/*  847 */       } catch (Exception e) {
/*      */ 
/*      */         
/*  850 */         e.printStackTrace();
/*      */       }  
/*  852 */     if (this.notifyCoilsChanged != null)
/*  853 */       this.notifyCoilsChanged.coilsChangedEvent(); 
/*      */   }
/*      */   
/*      */   private void WriteMultipleRegisters(Socket socket) {
/*      */     byte[] data;
/*  858 */     this.sendData = new ModbusProtocoll();
/*  859 */     this.sendData.response = true;
/*      */     
/*  861 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  862 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  864 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  865 */     this.sendData.functionCode = this.receiveData.functionCode;
/*  866 */     this.sendData.startingAdress = this.receiveData.startingAdress;
/*  867 */     this.sendData.quantity = this.receiveData.quantity;
/*      */     
/*  869 */     if ((((this.receiveData.quantity == 0) ? 1 : 0) | ((this.receiveData.quantity > 1968) ? 1 : 0)) != 0) {
/*      */       
/*  871 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 144);
/*  872 */       this.sendData.exceptionCode = 3;
/*      */     } 
/*  874 */     if (this.receiveData.startingAdress + 1 + this.receiveData.quantity > 65535) {
/*      */       
/*  876 */       this.sendData.errorCode = (byte)(this.receiveData.functionCode + 144);
/*  877 */       this.sendData.exceptionCode = 2;
/*      */     } 
/*  879 */     for (int i = 0; i < this.receiveData.quantity; i++)
/*      */     {
/*  881 */       this.holdingRegisters[this.receiveData.startingAdress + i + 1] = this.receiveData.receiveRegisterValues[i];
/*      */     }
/*  883 */     if (this.sendData.exceptionCode > 0) {
/*  884 */       this.sendData.length = 3;
/*      */     } else {
/*  886 */       this.sendData.length = 6;
/*      */     } 
/*      */     
/*  889 */     if (this.sendData.exceptionCode > 0) {
/*  890 */       data = new byte[9];
/*      */     } else {
/*  892 */       data = new byte[12];
/*      */     } 
/*  894 */     this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  897 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  898 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  901 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  902 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  905 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  906 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  909 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  912 */     data[7] = this.sendData.functionCode;
/*      */ 
/*      */ 
/*      */     
/*  916 */     if (this.sendData.exceptionCode > 0) {
/*      */       
/*  918 */       data[7] = this.sendData.errorCode;
/*  919 */       data[8] = this.sendData.exceptionCode;
/*  920 */       this.sendData.sendRegisterValues = null;
/*      */     }
/*      */     else {
/*      */       
/*  924 */       data[8] = (byte)((this.receiveData.startingAdress & 0xFF00) >> 8);
/*  925 */       data[9] = (byte)(this.receiveData.startingAdress & 0xFF);
/*      */       
/*  927 */       data[10] = (byte)((this.receiveData.quantity & 0xFF00) >> 8);
/*  928 */       data[11] = (byte)(this.receiveData.quantity & 0xFF);
/*      */     } 
/*      */     
/*  931 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0)
/*      */       try {
/*  933 */         OutputStream outputStream = socket.getOutputStream();
/*  934 */         outputStream.write(data);
/*  935 */       } catch (IOException e) {
/*      */         
/*  937 */         e.printStackTrace();
/*      */       }  
/*  939 */     if (this.notifyHoldingRegistersChanged != null) {
/*  940 */       this.notifyHoldingRegistersChanged.holdingRegistersChangedEvent();
/*      */     }
/*      */   }
/*      */   
/*      */   private void sendException(int errorCode, int exceptionCode, Socket socket) {
/*      */     byte[] data;
/*  946 */     this.sendData = new ModbusProtocoll();
/*  947 */     this.sendData.response = true;
/*      */     
/*  949 */     this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
/*  950 */     this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
/*      */     
/*  952 */     this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
/*  953 */     this.sendData.errorCode = (byte)errorCode;
/*  954 */     this.sendData.exceptionCode = (byte)exceptionCode;
/*      */     
/*  956 */     if (this.sendData.exceptionCode > 0) {
/*  957 */       this.sendData.length = 3;
/*      */     } else {
/*  959 */       this.sendData.length = (short)(3 + this.sendData.byteCount);
/*      */     } 
/*      */ 
/*      */     
/*  963 */     if (this.sendData.exceptionCode > 0) {
/*  964 */       data = new byte[9];
/*      */     } else {
/*  966 */       data = new byte[9 + this.sendData.byteCount];
/*  967 */     }  this.sendData.length = (byte)(data.length - 6);
/*      */ 
/*      */     
/*  970 */     data[0] = (byte)((this.sendData.transactionIdentifier & 0xFF00) >> 8);
/*  971 */     data[1] = (byte)(this.sendData.transactionIdentifier & 0xFF);
/*      */ 
/*      */     
/*  974 */     data[2] = (byte)((this.sendData.protocolIdentifier & 0xFF00) >> 8);
/*  975 */     data[3] = (byte)(this.sendData.protocolIdentifier & 0xFF);
/*      */ 
/*      */     
/*  978 */     data[4] = (byte)((this.sendData.length & 0xFF00) >> 8);
/*  979 */     data[5] = (byte)(this.sendData.length & 0xFF);
/*      */ 
/*      */     
/*  982 */     data[6] = this.sendData.unitIdentifier;
/*      */ 
/*      */     
/*  985 */     data[7] = this.sendData.errorCode;
/*  986 */     data[8] = this.sendData.exceptionCode;
/*      */     
/*  988 */     if (((socket.isConnected()?1:0) & (socket.isClosed() ? 0 : 1)) != 0) {
/*      */       try {
/*  990 */         OutputStream outputStream = socket.getOutputStream();
/*  991 */         outputStream.write(data);
/*  992 */       } catch (IOException e) {
/*      */         
/*  994 */         e.printStackTrace();
/*      */       } 
/*      */     }
/*      */   }
/*      */   
/*      */   protected void CreateLogData() {
/* 1000 */     for (int i = 0; i < 98; i++)
/*      */     {
/* 1002 */       this.modbusLogData[99 - i] = this.modbusLogData[99 - i - 2];
/*      */     }
/* 1004 */     this.modbusLogData[0] = this.receiveData;
/* 1005 */     this.modbusLogData[1] = this.sendData;
/* 1006 */     if (this.notifyLogDataChanged != null) {
/* 1007 */       this.notifyLogDataChanged.logDataChangedEvent();
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setPort(int port) {
/* 1016 */     this.port = port;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode1Disabled(boolean functionCode1Disabled) {
/* 1025 */     this.functionCode1Disabled = functionCode1Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode2Disabled(boolean functionCode2Disabled) {
/* 1035 */     this.functionCode2Disabled = functionCode2Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode3Disabled(boolean functionCode3Disabled) {
/* 1045 */     this.functionCode3Disabled = functionCode3Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode4Disabled(boolean functionCode4Disabled) {
/* 1055 */     this.functionCode4Disabled = functionCode4Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode5Disabled(boolean functionCode5Disabled) {
/* 1065 */     this.functionCode5Disabled = functionCode5Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode6Disabled(boolean functionCode6Disabled) {
/* 1075 */     this.functionCode6Disabled = functionCode6Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode15Disabled(boolean functionCode15Disabled) {
/* 1085 */     this.functionCode15Disabled = functionCode15Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setFunctionCode16Disabled(boolean functionCode16Disabled) {
/* 1095 */     this.functionCode16Disabled = functionCode16Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setNumberOfConnectedClients(int value) {
/* 1100 */     this.numberOfConnections = value;
/* 1101 */     if (this.notifyNumberOfConnectedClientsChanged != null) {
/* 1102 */       this.notifyNumberOfConnectedClientsChanged.NumberOfConnectedClientsChanged();
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int getPort() {
/* 1112 */     return this.port;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode1Disabled() {
/* 1117 */     return this.functionCode1Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode2Disabled() {
/* 1122 */     return this.functionCode2Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode3Disabled() {
/* 1127 */     return this.functionCode3Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode4Disabled() {
/* 1132 */     return this.functionCode4Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode5Disabled() {
/* 1137 */     return this.functionCode5Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode6Disabled() {
/* 1142 */     return this.functionCode6Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode15Disabled() {
/* 1147 */     return this.functionCode15Disabled;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getFunctionCode16Disabled() {
/* 1152 */     return this.functionCode16Disabled;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int getNumberOfConnectedClients() {
/* 1161 */     return this.numberOfConnections;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean getServerRunning() {
/* 1170 */     return this.serverRunning;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public ModbusProtocoll[] getLogData() {
/* 1179 */     return this.modbusLogData;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setNotifyCoilsChanged(ICoilsChangedDelegator value) {
/* 1188 */     this.notifyCoilsChanged = value;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setNotifyHoldingRegistersChanged(IHoldingRegistersChangedDelegator value) {
/* 1197 */     this.notifyHoldingRegistersChanged = value;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setNotifyNumberOfConnectedClientsChanged(INumberOfConnectedClientsChangedDelegator value) {
/* 1206 */     this.notifyNumberOfConnectedClientsChanged = value;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setNotifyLogDataChanged(ILogDataChangedDelegator value) {
/* 1215 */     this.notifyLogDataChanged = value;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int getClientConnectionTimeout() {
/* 1224 */     return this.clientConnectionTimeout;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setClientConnectionTimeout(int value) {
/* 1232 */     this.clientConnectionTimeout = value;
/*      */   }
/*      */ }


/* Location:              d:\libs\EasyModbusJava.jar!\de\re\easymodbus\server\ModbusServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */