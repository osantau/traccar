/*      */ package de.re.easymodbus.modbusclient;
/*      */ import java.io.DataOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.net.DatagramPacket;
/*      */ import java.net.DatagramSocket;
/*      */ import java.net.InetAddress;
/*      */ import java.net.Socket;
/*      */ import java.net.SocketException;
/*      */ import java.net.UnknownHostException;
/*      */ import java.nio.ByteBuffer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;

/*      */ 
/*      */ import de.re.easymodbus.exceptions.ConnectionException;
/*      */ import de.re.easymodbus.exceptions.FunctionCodeNotSupportedException;
/*      */ import de.re.easymodbus.exceptions.ModbusException;
/*      */ import de.re.easymodbus.exceptions.QuantityInvalidException;
/*      */ import de.re.easymodbus.exceptions.StartingAddressInvalidException;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class ModbusClient
/*      */ {
/*      */   public enum RegisterOrder
/*      */   {
/*   33 */     LowHigh, HighLow; }
/*   34 */   private Socket tcpClientSocket = new Socket();
/*   35 */   protected String ipAddress = "190.201.100.100";
/*   36 */   protected int port = 502;
/*   37 */   private byte[] transactionIdentifier = new byte[2];
/*   38 */   private byte[] protocolIdentifier = new byte[2];
/*   39 */   private byte[] length = new byte[2];
/*   40 */   private byte[] crc = new byte[2];
/*   41 */   private byte unitIdentifier = 1;
/*      */   private byte functionCode;
/*   43 */   private byte[] startingAddress = new byte[2];
/*   44 */   private byte[] quantity = new byte[2];
/*      */   private boolean udpFlag = false;
/*      */   private boolean serialflag = false;
/*   47 */   private int connectTimeout = 500;
/*      */   private InputStream inStream;
/*      */   private DataOutputStream outStream;
/*      */   public byte[] receiveData;
/*      */   public byte[] sendData;
/*   52 */   private List<ReceiveDataChangedListener> receiveDataChangedListener = new ArrayList<>();
/*   53 */   private List<SendDataChangedListener> sendDataChangedListener = new ArrayList<>();

/*      */   OutputStream out;
/*      */   
/*      */   public ModbusClient(String ipAddress, int port) {
/*             System.out.println("EasyModbus Client Library");
             System.out.println("Copyright (c) Stefan Rossmann Engineering Solutions");
             System.out.println("www.rossmann-engineering.de");
             System.out.println("");
             System.out.println("Creative commons license");
        System.out.println("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)"); 
    */
/*   64 */     this.ipAddress = ipAddress;
/*   65 */     this.port = port;
/*      */   }
/*      */   InputStream in; 
/*      */   
/*      */   public ModbusClient() {
/*   70 */     System.out.println("EasyModbus Client Library");
/*   71 */     System.out.println("Copyright (c) Stefan Rossmann Engineering Solutions");
/*   72 */     System.out.println("www.rossmann-engineering.de");
/*   73 */     System.out.println("");
/*   74 */     System.out.println("Creative commons license");
/*   75 */     System.out.println("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void Connect() throws UnknownHostException, IOException {
/*   85 */     if (!this.udpFlag) {
/*      */ 
/*      */       
/*   88 */       this.tcpClientSocket = new Socket(this.ipAddress, this.port);
/*   89 */       this.tcpClientSocket.setSoTimeout(this.connectTimeout);
/*   90 */       this.outStream = new DataOutputStream(this.tcpClientSocket.getOutputStream());
/*   91 */       this.inStream = this.tcpClientSocket.getInputStream();
/*      */     } 
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
/*      */   public void Connect(String ipAddress, int port) throws UnknownHostException, IOException {
/*  104 */     this.ipAddress = ipAddress;
/*  105 */     this.port = port;
/*      */     
/*  107 */     this.tcpClientSocket = new Socket(ipAddress, port);
/*  108 */     this.tcpClientSocket.setSoTimeout(this.connectTimeout);
/*  109 */     this.outStream = new DataOutputStream(this.tcpClientSocket.getOutputStream());
/*  110 */     this.inStream = this.tcpClientSocket.getInputStream();
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
/*      */   public static float ConvertRegistersToFloat(int[] registers) throws IllegalArgumentException {
/*  158 */     if (registers.length != 2)
/*  159 */       throw new IllegalArgumentException("Input Array length invalid"); 
/*  160 */     int highRegister = registers[1];
/*  161 */     int lowRegister = registers[0];
/*  162 */     byte[] highRegisterBytes = toByteArray(highRegister);
/*  163 */     byte[] lowRegisterBytes = toByteArray(lowRegister);
/*  164 */     byte[] floatBytes = {
/*  165 */         highRegisterBytes[1], 
/*  166 */         highRegisterBytes[0], 
/*  167 */         lowRegisterBytes[1], 
/*  168 */         lowRegisterBytes[0]
/*      */       };
/*  170 */     return ByteBuffer.wrap(floatBytes).getFloat();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static double ConvertRegistersToDoublePrecisionFloat(int[] registers) throws IllegalArgumentException {
/*  180 */     if (registers.length != 4)
/*  181 */       throw new IllegalArgumentException("Input Array length invalid"); 
/*  182 */     byte[] highRegisterBytes = toByteArray(registers[3]);
/*  183 */     byte[] highLowRegisterBytes = toByteArray(registers[2]);
/*  184 */     byte[] lowHighRegisterBytes = toByteArray(registers[1]);
/*  185 */     byte[] lowRegisterBytes = toByteArray(registers[0]);
/*  186 */     byte[] doubleBytes = {
/*  187 */         highRegisterBytes[1], 
/*  188 */         highRegisterBytes[0], 
/*  189 */         highLowRegisterBytes[1], 
/*  190 */         highLowRegisterBytes[0], 
/*  191 */         lowHighRegisterBytes[1], 
/*  192 */         lowHighRegisterBytes[0], 
/*  193 */         lowRegisterBytes[1], 
/*  194 */         lowRegisterBytes[0]
/*      */       };
/*  196 */     return ByteBuffer.wrap(doubleBytes).getDouble();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static double ConvertRegistersToDoublePrecisionFloat(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
/*  207 */     if (registers.length != 4)
/*  208 */       throw new IllegalArgumentException("Input Array length invalid"); 
/*  209 */     int[] swappedRegisters = { registers[0], registers[1], registers[2], registers[3] };
/*  210 */     if (registerOrder == RegisterOrder.HighLow)
/*  211 */       swappedRegisters = new int[] { registers[3], registers[2], registers[1], registers[0] }; 
/*  212 */     return ConvertRegistersToDoublePrecisionFloat(swappedRegisters);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static float ConvertRegistersToFloat(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
/*  223 */     int[] swappedRegisters = { registers[0], registers[1] };
/*  224 */     if (registerOrder == RegisterOrder.HighLow)
/*  225 */       swappedRegisters = new int[] { registers[1], registers[0] }; 
/*  226 */     return ConvertRegistersToFloat(swappedRegisters);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static long ConvertRegistersToLong(int[] registers) throws IllegalArgumentException {
/*  237 */     if (registers.length != 4)
/*  238 */       throw new IllegalArgumentException("Input Array length invalid"); 
/*  239 */     byte[] highRegisterBytes = toByteArray(registers[3]);
/*  240 */     byte[] highLowRegisterBytes = toByteArray(registers[2]);
/*  241 */     byte[] lowHighRegisterBytes = toByteArray(registers[1]);
/*  242 */     byte[] lowRegisterBytes = toByteArray(registers[0]);
/*  243 */     byte[] longBytes = {
/*  244 */         highRegisterBytes[1], 
/*  245 */         highRegisterBytes[0], 
/*  246 */         highLowRegisterBytes[1], 
/*  247 */         highLowRegisterBytes[0], 
/*  248 */         lowHighRegisterBytes[1], 
/*  249 */         lowHighRegisterBytes[0], 
/*  250 */         lowRegisterBytes[1], 
/*  251 */         lowRegisterBytes[0]
/*      */       };
/*  253 */     return ByteBuffer.wrap(longBytes).getLong();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static long ConvertRegistersToLong(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
/*  263 */     if (registers.length != 4)
/*  264 */       throw new IllegalArgumentException("Input Array length invalid"); 
/*  265 */     int[] swappedRegisters = { registers[0], registers[1], registers[2], registers[3] };
/*  266 */     if (registerOrder == RegisterOrder.HighLow)
/*  267 */       swappedRegisters = new int[] { registers[3], registers[2], registers[1], registers[0] }; 
/*  268 */     return ConvertRegistersToLong(swappedRegisters);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int ConvertRegistersToDouble(int[] registers) throws IllegalArgumentException {
/*  278 */     if (registers.length != 2)
/*  279 */       throw new IllegalArgumentException("Input Array length invalid"); 
/*  280 */     int highRegister = registers[1];
/*  281 */     int lowRegister = registers[0];
/*  282 */     byte[] highRegisterBytes = toByteArray(highRegister);
/*  283 */     byte[] lowRegisterBytes = toByteArray(lowRegister);
/*  284 */     byte[] doubleBytes = {
/*  285 */         highRegisterBytes[1], 
/*  286 */         highRegisterBytes[0], 
/*  287 */         lowRegisterBytes[1], 
/*  288 */         lowRegisterBytes[0]
/*      */       };
/*  290 */     return ByteBuffer.wrap(doubleBytes).getInt();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int ConvertRegistersToDouble(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
/*  301 */     int[] swappedRegisters = { registers[0], registers[1] };
/*  302 */     if (registerOrder == RegisterOrder.HighLow)
/*  303 */       swappedRegisters = new int[] { registers[1], registers[0] }; 
/*  304 */     return ConvertRegistersToDouble(swappedRegisters);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int[] ConvertFloatToTwoRegisters(float floatValue) {
/*  314 */     byte[] floatBytes = toByteArray(floatValue);
/*  315 */     byte[] highRegisterBytes = {
/*      */ 
/*      */         
/*  318 */         0, 0, floatBytes[0], 
/*  319 */         floatBytes[1]
/*      */       };
/*      */     
/*  322 */     byte[] lowRegisterBytes = {
/*      */ 
/*      */         
/*  325 */         0, 0, floatBytes[2], 
/*  326 */         floatBytes[3]
/*      */       };
/*      */     
/*  329 */     int[] returnValue = {
/*      */         
/*  331 */         ByteBuffer.wrap(lowRegisterBytes).getInt(), 
/*  332 */         ByteBuffer.wrap(highRegisterBytes).getInt()
/*      */       };
/*  334 */     return returnValue;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int[] ConvertFloatToTwoRegisters(float floatValue, RegisterOrder registerOrder) {
/*  345 */     int[] registerValues = ConvertFloatToTwoRegisters(floatValue);
/*  346 */     int[] returnValue = registerValues;
/*  347 */     if (registerOrder == RegisterOrder.HighLow)
/*  348 */       returnValue = new int[] { registerValues[1], registerValues[0] }; 
/*  349 */     return returnValue;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int[] ConvertDoubleToTwoRegisters(int doubleValue) {
/*  359 */     byte[] doubleBytes = toByteArrayDouble(doubleValue);
/*  360 */     byte[] highRegisterBytes = {
/*      */ 
/*      */         
/*  363 */         0, 0, doubleBytes[0], 
/*  364 */         doubleBytes[1]
/*      */       };
/*      */     
/*  367 */     byte[] lowRegisterBytes = {
/*      */ 
/*      */         
/*  370 */         0, 0, doubleBytes[2], 
/*  371 */         doubleBytes[3]
/*      */       };
/*      */     
/*  374 */     int[] returnValue = {
/*      */         
/*  376 */         ByteBuffer.wrap(lowRegisterBytes).getInt(), 
/*  377 */         ByteBuffer.wrap(highRegisterBytes).getInt()
/*      */       };
/*  379 */     return returnValue;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int[] ConvertDoubleToTwoRegisters(int doubleValue, RegisterOrder registerOrder) {
/*  390 */     int[] registerValues = ConvertFloatToTwoRegisters(doubleValue);
/*  391 */     int[] returnValue = registerValues;
/*  392 */     if (registerOrder == RegisterOrder.HighLow)
/*  393 */       returnValue = new int[] { registerValues[1], registerValues[0] }; 
/*  394 */     return returnValue;
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
/*      */   public static String ConvertRegistersToString(int[] registers, int offset, int stringLength) {
/*  406 */     byte[] result = new byte[stringLength];
/*  407 */     byte[] registerResult = new byte[2];
/*      */     
/*  409 */     for (int i = 0; i < stringLength / 2; i++) {
/*      */       
/*  411 */       registerResult = toByteArray(registers[offset + i]);
/*  412 */       result[i * 2] = registerResult[0];
/*  413 */       result[i * 2 + 1] = registerResult[1];
/*      */     } 
/*  415 */     return new String(result);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int[] ConvertStringToRegisters(String stringToConvert) {
/*  425 */     byte[] array = stringToConvert.getBytes();
/*  426 */     int[] returnarray = new int[stringToConvert.length() / 2 + stringToConvert.length() % 2];
/*  427 */     for (int i = 0; i < returnarray.length; i++) {
/*      */       
/*  429 */       returnarray[i] = array[i * 2];
/*  430 */       if (i * 2 + 1 < array.length)
/*      */       {
/*  432 */         returnarray[i] = returnarray[i] | array[i * 2 + 1] << 8;
/*      */       }
/*      */     } 
/*  435 */     return returnarray;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] calculateCRC(byte[] data, int numberOfBytes, int startByte) {
/*  441 */     byte[] auchCRCHi = { 
/*  442 */         0, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 
/*  443 */         64, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, -63, -127, 64, 1, -64, Byte
/*  444 */         .MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, -63, -127, 64, 1, 
/*  445 */         -64, Byte.MIN_VALUE, 65, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, 
/*  446 */         -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, -63, -127, 
/*  447 */         64, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, -64, 
/*  448 */         Byte.MIN_VALUE, 65, -63, -127, 64, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, 
/*  449 */         -64, Byte.MIN_VALUE, 65, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, 
/*  450 */         -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 
/*  451 */         64, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, 1, -64, Byte
/*  452 */         .MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, -63, -127, 64, 1, 
/*  453 */         -64, Byte.MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 
/*  454 */         -63, -127, 64, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 
/*  455 */         64, 1, -64, Byte.MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 64, 1, -64, Byte
/*  456 */         .MIN_VALUE, 65, -63, -127, 64, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, 
/*  457 */         -64, Byte.MIN_VALUE, 65, -63, -127, 64, -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 
/*  458 */         -63, -127, 64, 1, -64, Byte.MIN_VALUE, 65, 1, -64, Byte.MIN_VALUE, 65, -63, -127, 
/*  459 */         64 };
/*      */ 
/*      */     
/*  462 */     byte[] auchCRCLo = { 
/*  463 */         0, -64, -63, 1, -61, 3, 2, -62, -58, 6, 7, -57, 5, -59, -60, 
/*  464 */         4, -52, 12, 13, -51, 15, -49, -50, 14, 10, -54, -53, 11, -55, 9, 
/*  465 */         8, -56, -40, 24, 25, -39, 27, -37, -38, 26, 30, -34, -33, 31, -35, 
/*  466 */         29, 28, -36, 20, -44, -43, 21, -41, 23, 22, -42, -46, 18, 19, -45, 
/*  467 */         17, -47, -48, 16, -16, 48, 49, -15, 51, -13, -14, 50, 54, -10, -9, 
/*  468 */         55, -11, 53, 52, -12, 60, -4, -3, 61, -1, 63, 62, -2, -6, 58, 
/*  469 */         59, -5, 57, -7, -8, 56, 40, -24, -23, 41, -21, 43, 42, -22, -18, 
/*  470 */         46, 47, -17, 45, -19, -20, 44, -28, 36, 37, -27, 39, -25, -26, 38, 
/*  471 */         34, -30, -29, 35, -31, 33, 32, -32, -96, 96, 97, -95, 99, -93, -94, 
/*  472 */         98, 102, -90, -89, 103, -91, 101, 100, -92, 108, -84, -83, 109, -81, 111, 
/*  473 */         110, -82, -86, 106, 107, -85, 105, -87, -88, 104, 120, -72, -71, 121, -69, 
/*  474 */         123, 122, -70, -66, 126, Byte.MAX_VALUE, -65, 125, -67, -68, 124, -76, 116, 117, -75, 
/*  475 */         119, -73, -74, 118, 114, -78, -77, 115, -79, 113, 112, -80, 80, -112, -111, 
/*  476 */         81, -109, 83, 82, -110, -106, 86, 87, -105, 85, -107, -108, 84, -100, 92, 
/*  477 */         93, -99, 95, -97, -98, 94, 90, -102, -101, 91, -103, 89, 88, -104, -120, 
/*  478 */         72, 73, -119, 75, -117, -118, 74, 78, -114, -113, 79, -115, 77, 76, -116, 
/*  479 */         68, -124, -123, 69, -121, 71, 70, -122, -126, 66, 67, -125, 65, -127, Byte.MIN_VALUE, 
/*  480 */         64 };
/*      */     
/*  482 */     short usDataLen = (short)numberOfBytes;
/*  483 */     byte uchCRCHi = -1;
/*  484 */     byte uchCRCLo = -1;
/*  485 */     int i = 0;
/*      */     
/*  487 */     while (usDataLen > 0) {
/*      */       
/*  489 */       usDataLen = (short)(usDataLen - 1);
/*  490 */       int uIndex = uchCRCLo ^ data[i + startByte];
/*  491 */       if (uIndex < 0)
/*  492 */         uIndex += 256; 
/*  493 */       uchCRCLo = (byte)(uchCRCHi ^ auchCRCHi[uIndex]);
/*  494 */       uchCRCHi = auchCRCLo[uIndex];
/*  495 */       i++;
/*      */     } 
/*  497 */     byte[] returnValue = { uchCRCLo, uchCRCHi };
/*  498 */     return returnValue;
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
/*      */   public boolean[] ReadDiscreteInputs(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
/*  514 */     if (this.tcpClientSocket == null)
/*  515 */       throw new ConnectionException("connection Error"); 
/*  516 */     if ((((startingAddress > 65535) ? 1 : 0) | ((quantity > 2000) ? 1 : 0)) != 0)
/*  517 */       throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 2000"); 
/*  518 */     boolean[] response = null;
/*  519 */     this.transactionIdentifier = toByteArray(1);
/*  520 */     this.protocolIdentifier = toByteArray(0);
/*  521 */     this.length = toByteArray(6);
/*  522 */     this.functionCode = 2;
/*  523 */     this.startingAddress = toByteArray(startingAddress);
/*  524 */     this.quantity = toByteArray(quantity);
/*  525 */     byte[] data = {
/*      */         
/*  527 */         this.transactionIdentifier[1], 
/*  528 */         this.transactionIdentifier[0], 
/*  529 */         this.protocolIdentifier[1], 
/*  530 */         this.protocolIdentifier[0], 
/*  531 */         this.length[1], 
/*  532 */         this.length[0], 
/*  533 */         this.unitIdentifier, 
/*  534 */         this.functionCode, 
/*  535 */         this.startingAddress[1], 
/*  536 */         this.startingAddress[0], 
/*  537 */         this.quantity[1], 
/*  538 */         this.quantity[0], 
/*  539 */         this.crc[0], 
/*  540 */         this.crc[1]
/*      */       };
/*  542 */     if (this.serialflag) {
/*      */       
/*  544 */       this.crc = calculateCRC(data, 6, 6);
/*  545 */       data[data.length - 2] = this.crc[0];
/*  546 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/*  548 */     byte[] serialdata = null;
/*  549 */     if (this.serialflag) {
/*      */ 
/*      */       
/*  552 */       this.out.write(data, 6, 8);
/*  553 */       long dateTimeSend = DateTime.getDateTimeTicks();
/*  554 */       byte receivedUnitIdentifier = -1;
/*  555 */       int len = -1;
/*  556 */       byte[] serialBuffer = new byte[256];
/*  557 */       serialdata = new byte[256];
/*  558 */       int expectedlength = 5 + quantity / 8 + 1;
/*  559 */       if (quantity % 8 == 0)
/*  560 */         expectedlength = 5 + quantity / 8; 
/*  561 */       int currentLength = 0; while (true) {
/*  562 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/*  564 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/*  566 */           len = -1; do {
/*      */           
/*  568 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/*  570 */           for (int j = 0; j < len; j++) {
/*      */             
/*  572 */             serialdata[currentLength] = serialBuffer[j];
/*  573 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/*  577 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/*  579 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/*  581 */         serialdata = new byte[256];
/*      */       }
/*      */     } 
/*  584 */     if (serialdata != null) {
/*      */       
/*  586 */       data = new byte[262];
/*  587 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/*      */     
/*  590 */     if ((this.tcpClientSocket.isConnected() | this.udpFlag))
/*      */     {
/*  592 */       if (this.udpFlag) {
/*      */         
/*  594 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/*  595 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length - 2, ipAddress, this.port);
/*  596 */         DatagramSocket clientSocket = new DatagramSocket();
/*  597 */         clientSocket.setSoTimeout(500);
/*  598 */         clientSocket.send(sendPacket);
/*  599 */         data = new byte[2100];
/*  600 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/*  601 */         clientSocket.receive(receivePacket);
/*  602 */         clientSocket.close();
/*  603 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/*  607 */         this.outStream.write(data, 0, data.length - 2);
/*  608 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/*  610 */           this.sendData = new byte[data.length - 2];
/*  611 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/*  612 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/*  613 */             hl.SendDataChanged(); 
/*      */         } 
/*  615 */         data = new byte[2100];
/*  616 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/*  617 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/*  619 */           this.receiveData = new byte[numberOfBytes];
/*  620 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/*  621 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/*  622 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/*  626 */     if (((((data[7] & 0xFF) == 130) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/*  627 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/*  628 */     if (((((data[7] & 0xFF) == 130) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/*  629 */       throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid"); 
/*  630 */     if (((((data[7] & 0xFF) == 130) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/*  631 */       throw new QuantityInvalidException("Quantity invalid"); 
/*  632 */     if (((((data[7] & 0xFF) == 130) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0)
/*  633 */       throw new ModbusException("Error reading"); 
/*  634 */     response = new boolean[quantity];
/*  635 */     for (int i = 0; i < quantity; i++) {
/*      */       
/*  637 */       int intData = data[9 + i / 8];
/*  638 */       int mask = (int)Math.pow(2.0D, (i % 8));
/*  639 */       intData = (intData & mask) / mask;
/*  640 */       if (intData > 0) {
/*  641 */         response[i] = true;
/*      */       } else {
/*  643 */         response[i] = false;
/*      */       } 
/*      */     } 
/*      */     
/*  647 */     return response;
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
/*      */   public boolean[] ReadCoils(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
/*  662 */     if (this.tcpClientSocket == null)
/*  663 */       throw new ConnectionException("connection Error"); 
/*  664 */     if ((((startingAddress > 65535) ? 1 : 0) | ((quantity > 2000) ? 1 : 0)) != 0)
/*  665 */       throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 2000"); 
/*  666 */     boolean[] response = new boolean[quantity];
/*  667 */     this.transactionIdentifier = toByteArray(1);
/*  668 */     this.protocolIdentifier = toByteArray(0);
/*  669 */     this.length = toByteArray(6);
/*      */     
/*  671 */     this.functionCode = 1;
/*  672 */     this.startingAddress = toByteArray(startingAddress);
/*  673 */     this.quantity = toByteArray(quantity);
/*  674 */     byte[] data = {
/*      */         
/*  676 */         this.transactionIdentifier[1], 
/*  677 */         this.transactionIdentifier[0], 
/*  678 */         this.protocolIdentifier[1], 
/*  679 */         this.protocolIdentifier[0], 
/*  680 */         this.length[1], 
/*  681 */         this.length[0], 
/*  682 */         this.unitIdentifier, 
/*  683 */         this.functionCode, 
/*  684 */         this.startingAddress[1], 
/*  685 */         this.startingAddress[0], 
/*  686 */         this.quantity[1], 
/*  687 */         this.quantity[0], 
/*  688 */         this.crc[0], 
/*  689 */         this.crc[1]
/*      */       };
/*  691 */     if (this.serialflag) {
/*      */       
/*  693 */       this.crc = calculateCRC(data, 6, 6);
/*  694 */       data[data.length - 2] = this.crc[0];
/*  695 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/*  697 */     byte[] serialdata = null;
/*  698 */     if (this.serialflag) {
/*      */ 
/*      */       
/*  701 */       this.out.write(data, 6, 8);
/*  702 */       long dateTimeSend = DateTime.getDateTimeTicks();
/*  703 */       byte receivedUnitIdentifier = -1;
/*  704 */       int len = -1;
/*  705 */       byte[] serialBuffer = new byte[256];
/*  706 */       serialdata = new byte[256];
/*  707 */       int expectedlength = 5 + quantity / 8 + 1;
/*  708 */       if (quantity % 8 == 0)
/*  709 */         expectedlength = 5 + quantity / 8; 
/*  710 */       int currentLength = 0; while (true) {
/*  711 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/*  713 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/*  715 */           len = -1; do {
/*      */           
/*  717 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/*  719 */           for (int j = 0; j < len; j++) {
/*      */             
/*  721 */             serialdata[currentLength] = serialBuffer[j];
/*  722 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/*  726 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/*  728 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/*  730 */         serialdata = new byte[256];
/*      */       }
/*      */     } 
/*  733 */     if (serialdata != null) {
/*      */       
/*  735 */       data = new byte[262];
/*  736 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/*  738 */     if ((this.tcpClientSocket.isConnected() | this.udpFlag))
/*      */     {
/*  740 */       if (this.udpFlag) {
/*      */         
/*  742 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/*  743 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/*  744 */         DatagramSocket clientSocket = new DatagramSocket();
/*  745 */         clientSocket.setSoTimeout(500);
/*  746 */         clientSocket.send(sendPacket);
/*  747 */         data = new byte[2100];
/*  748 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/*  749 */         clientSocket.receive(receivePacket);
/*  750 */         clientSocket.close();
/*  751 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/*  755 */         this.outStream.write(data, 0, data.length - 2);
/*  756 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/*  758 */           this.sendData = new byte[data.length - 2];
/*  759 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/*  760 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/*  761 */             hl.SendDataChanged(); 
/*      */         } 
/*  763 */         data = new byte[2100];
/*  764 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/*  765 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/*  767 */           this.receiveData = new byte[numberOfBytes];
/*  768 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/*  769 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/*  770 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/*  774 */     if (((((data[7] & 0xFF) == 129) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/*  775 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/*  776 */     if (((((data[7] & 0xFF) == 129) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/*  777 */       throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid"); 
/*  778 */     if (((((data[7] & 0xFF) == 129) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/*  779 */       throw new QuantityInvalidException("Quantity invalid"); 
/*  780 */     if (((((data[7] & 0xFF) == 129) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0)
/*  781 */       throw new ModbusException("Error reading"); 
/*  782 */     for (int i = 0; i < quantity; i++) {
/*      */       
/*  784 */       int intData = data[9 + i / 8];
/*  785 */       int mask = (int)Math.pow(2.0D, (i % 8));
/*  786 */       intData = (intData & mask) / mask;
/*  787 */       if (intData > 0) {
/*  788 */         response[i] = true;
/*      */       } else {
/*  790 */         response[i] = false;
/*      */       } 
/*      */     } 
/*      */     
/*  794 */     return response;
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
/*      */   public int[] ReadHoldingRegisters(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
/*  809 */     if (this.tcpClientSocket == null)
/*  810 */       throw new ConnectionException("connection Error"); 
/*  811 */     if ((((startingAddress > 65535) ? 1 : 0) | ((quantity > 125) ? 1 : 0)) != 0)
/*  812 */       throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125"); 
/*  813 */     int[] response = new int[quantity];
/*  814 */     this.transactionIdentifier = toByteArray(1);
/*  815 */     this.protocolIdentifier = toByteArray(0);
/*  816 */     this.length = toByteArray(6);
/*      */     
/*  818 */     this.functionCode = 3;
/*  819 */     this.startingAddress = toByteArray(startingAddress);
/*  820 */     this.quantity = toByteArray(quantity);
/*      */     
/*  822 */     byte[] data = {
/*      */         
/*  824 */         this.transactionIdentifier[1], 
/*  825 */         this.transactionIdentifier[0], 
/*  826 */         this.protocolIdentifier[1], 
/*  827 */         this.protocolIdentifier[0], 
/*  828 */         this.length[1], 
/*  829 */         this.length[0], 
/*  830 */         this.unitIdentifier, 
/*  831 */         this.functionCode, 
/*  832 */         this.startingAddress[1], 
/*  833 */         this.startingAddress[0], 
/*  834 */         this.quantity[1], 
/*  835 */         this.quantity[0], 
/*  836 */         this.crc[0], 
/*  837 */         this.crc[1]
/*      */       };
/*      */     
/*  840 */     if (this.serialflag) {
/*      */       
/*  842 */       this.crc = calculateCRC(data, 6, 6);
/*  843 */       data[data.length - 2] = this.crc[0];
/*  844 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/*  846 */     byte[] serialdata = null;
/*  847 */     if (this.serialflag) {
/*      */       
/*  849 */       this.out.write(data, 6, 8);
/*  850 */       long dateTimeSend = DateTime.getDateTimeTicks();
/*  851 */       byte receivedUnitIdentifier = -1;
/*  852 */       int len = -1;
/*  853 */       byte[] serialBuffer = new byte[256];
/*  854 */       serialdata = new byte[256];
/*  855 */       int expectedlength = 5 + 2 * quantity;
/*  856 */       int currentLength = 0; while (true) {
/*  857 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/*  859 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/*  861 */           len = -1; do {
/*      */           
/*  863 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/*  865 */           for (int k = 0; k < len; k++) {
/*      */             
/*  867 */             serialdata[currentLength] = serialBuffer[k];
/*  868 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/*  872 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/*  874 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/*  876 */         data = new byte[256];
/*      */       }
/*  878 */       if (serialdata != null) {
/*      */         
/*  880 */         data = new byte[262];
/*  881 */         System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */       } 
/*  883 */       for (int j = 0; j < quantity; j++) {
/*      */         
/*  885 */         byte[] bytes = new byte[2];
/*  886 */         bytes[0] = data[3 + j * 2];
/*  887 */         bytes[1] = data[3 + j * 2 + 1];
/*  888 */         ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
/*  889 */         response[j] = byteBuffer.getShort();
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  894 */     if (this.tcpClientSocket.isConnected() | this.udpFlag)
/*      */     {
/*  896 */       if (this.udpFlag) {
/*      */         
/*  898 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/*  899 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/*  900 */         DatagramSocket clientSocket = new DatagramSocket();
/*  901 */         clientSocket.setSoTimeout(500);
/*  902 */         clientSocket.send(sendPacket);
/*  903 */         data = new byte[2100];
/*  904 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/*  905 */         clientSocket.receive(receivePacket);
/*  906 */         clientSocket.close();
/*  907 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/*  911 */         this.outStream.write(data, 0, data.length - 2);
/*  912 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/*  914 */           this.sendData = new byte[data.length - 2];
/*  915 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/*  916 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/*  917 */             hl.SendDataChanged(); 
/*      */         } 
/*  919 */         data = new byte[2100];
/*  920 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/*  921 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/*  923 */           this.receiveData = new byte[numberOfBytes];
/*  924 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/*  925 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/*  926 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/*  930 */     if ((((data[7] == 131) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/*  931 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/*  932 */     if ((((data[7] == 131) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/*  933 */       throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid"); 
/*  934 */     if ((((data[7] == 131) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/*  935 */       throw new QuantityInvalidException("Quantity invalid"); 
/*  936 */     if ((((data[7] == 131) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0)
/*  937 */       throw new ModbusException("Error reading"); 
/*  938 */     for (int i = 0; i < quantity; i++) {
/*      */       
/*  940 */       byte[] bytes = new byte[2];
/*  941 */       bytes[0] = data[9 + i * 2];
/*  942 */       bytes[1] = data[9 + i * 2 + 1];
/*  943 */       ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
/*      */       
/*  945 */       response[i] = byteBuffer.getShort();
/*      */     } 
/*      */ 
/*      */     
/*  949 */     return response;
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
/*      */   public int[] ReadInputRegisters(int startingAddress, int quantity) throws ModbusException, UnknownHostException, SocketException, IOException {
/*  964 */     if (this.tcpClientSocket == null)
/*  965 */       throw new ConnectionException("connection Error"); 
/*  966 */     if ((((startingAddress > 65535) ? 1 : 0) | ((quantity > 125) ? 1 : 0)) != 0)
/*  967 */       throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125"); 
/*  968 */     int[] response = new int[quantity];
/*  969 */     this.transactionIdentifier = toByteArray(1);
/*  970 */     this.protocolIdentifier = toByteArray(0);
/*  971 */     this.length = toByteArray(6);
/*      */     
/*  973 */     this.functionCode = 4;
/*  974 */     this.startingAddress = toByteArray(startingAddress);
/*  975 */     this.quantity = toByteArray(quantity);
/*  976 */     byte[] data = {
/*      */         
/*  978 */         this.transactionIdentifier[1], 
/*  979 */         this.transactionIdentifier[0], 
/*  980 */         this.protocolIdentifier[1], 
/*  981 */         this.protocolIdentifier[0], 
/*  982 */         this.length[1], 
/*  983 */         this.length[0], 
/*  984 */         this.unitIdentifier, 
/*  985 */         this.functionCode, 
/*  986 */         this.startingAddress[1], 
/*  987 */         this.startingAddress[0], 
/*  988 */         this.quantity[1], 
/*  989 */         this.quantity[0], 
/*  990 */         this.crc[0], 
/*  991 */         this.crc[1]
/*      */       };
/*  993 */     if (this.serialflag) {
/*      */       
/*  995 */       this.crc = calculateCRC(data, 6, 6);
/*  996 */       data[data.length - 2] = this.crc[0];
/*  997 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/*  999 */     byte[] serialdata = null;
/* 1000 */     if (this.serialflag) {
/*      */       
/* 1002 */       this.out.write(data, 6, 8);
/* 1003 */       long dateTimeSend = DateTime.getDateTimeTicks();
/* 1004 */       byte receivedUnitIdentifier = -1;
/* 1005 */       int len = -1;
/* 1006 */       byte[] serialBuffer = new byte[256];
/* 1007 */       serialdata = new byte[256];
/* 1008 */       int expectedlength = 5 + 2 * quantity;
/* 1009 */       int currentLength = 0; while (true) {
/* 1010 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/* 1012 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/* 1014 */           len = -1; do {
/*      */           
/* 1016 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/* 1018 */           for (int k = 0; k < len; k++) {
/*      */             
/* 1020 */             serialdata[currentLength] = serialBuffer[k];
/* 1021 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */ 
/*      */         
/* 1026 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/* 1028 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/* 1030 */         data = new byte[256];
/*      */       }
/* 1032 */       if (serialdata != null) {
/*      */         
/* 1034 */         data = new byte[262];
/* 1035 */         System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */       } 
/* 1037 */       for (int j = 0; j < quantity; j++) {
/*      */         
/* 1039 */         byte[] bytes = new byte[2];
/* 1040 */         bytes[0] = data[3 + j * 2];
/* 1041 */         bytes[1] = data[3 + j * 2 + 1];
/* 1042 */         ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
/* 1043 */         response[j] = byteBuffer.getShort();
/*      */       } 
/*      */     } 
/*      */     
/* 1047 */     if (this.tcpClientSocket.isConnected() | this.udpFlag) {
/*      */       
/* 1049 */       if (this.udpFlag) {
/*      */         
/* 1051 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/* 1052 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/* 1053 */         DatagramSocket clientSocket = new DatagramSocket();
/* 1054 */         clientSocket.setSoTimeout(500);
/* 1055 */         clientSocket.send(sendPacket);
/* 1056 */         data = new byte[2100];
/* 1057 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/* 1058 */         clientSocket.receive(receivePacket);
/* 1059 */         clientSocket.close();
/* 1060 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/* 1064 */         this.outStream.write(data, 0, data.length - 2);
/* 1065 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/* 1067 */           this.sendData = new byte[data.length - 2];
/* 1068 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/* 1069 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/* 1070 */             hl.SendDataChanged(); 
/*      */         } 
/* 1072 */         data = new byte[2100];
/* 1073 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/* 1074 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/* 1076 */           this.receiveData = new byte[numberOfBytes];
/* 1077 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/* 1078 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/* 1079 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/* 1082 */       if (((((data[7] & 0xFF) == 132) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/* 1083 */         throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/* 1084 */       if (((((data[7] & 0xFF) == 132) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/* 1085 */         throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid"); 
/* 1086 */       if (((((data[7] & 0xFF) == 132) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/* 1087 */         throw new QuantityInvalidException("Quantity invalid"); 
/* 1088 */       if (((((data[7] & 0xFF) == 132) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0)
/* 1089 */         throw new ModbusException("Error reading"); 
/*      */     } 
/* 1091 */     for (int i = 0; i < quantity; i++) {
/*      */       
/* 1093 */       byte[] bytes = new byte[2];
/* 1094 */       bytes[0] = data[9 + i * 2];
/* 1095 */       bytes[1] = data[9 + i * 2 + 1];
/* 1096 */       ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
/* 1097 */       response[i] = byteBuffer.getShort();
/*      */     } 
/*      */ 
/*      */     
/* 1101 */     return response;
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
/*      */   public void WriteSingleCoil(int startingAddress, boolean value) throws ModbusException, UnknownHostException, SocketException, IOException {
/* 1115 */     if ((((this.tcpClientSocket == null) ? 1 : 0) & (this.udpFlag ? 0 : 1)) != 0)
/* 1116 */       throw new ConnectionException("connection error"); 
/* 1117 */     byte[] coilValue = new byte[2];
/* 1118 */     this.transactionIdentifier = toByteArray(1);
/* 1119 */     this.protocolIdentifier = toByteArray(0);
/* 1120 */     this.length = toByteArray(6);
/*      */     
/* 1122 */     this.functionCode = 5;
/* 1123 */     this.startingAddress = toByteArray(startingAddress);
/* 1124 */     if (value) {
/*      */       
/* 1126 */       coilValue = toByteArray(65280);
/*      */     }
/*      */     else {
/*      */       
/* 1130 */       coilValue = toByteArray(0);
/*      */     } 
/* 1132 */     byte[] data = { this.transactionIdentifier[1], 
/* 1133 */         this.transactionIdentifier[0], 
/* 1134 */         this.protocolIdentifier[1], 
/* 1135 */         this.protocolIdentifier[0], 
/* 1136 */         this.length[1], 
/* 1137 */         this.length[0], 
/* 1138 */         this.unitIdentifier, 
/* 1139 */         this.functionCode, 
/* 1140 */         this.startingAddress[1], 
/* 1141 */         this.startingAddress[0], 
/* 1142 */         coilValue[1], 
/* 1143 */         coilValue[0], 
/* 1144 */         this.crc[0], 
/* 1145 */         this.crc[1] };
/*      */     
/* 1147 */     if (this.serialflag) {
/*      */       
/* 1149 */       this.crc = calculateCRC(data, 6, 6);
/* 1150 */       data[data.length - 2] = this.crc[0];
/* 1151 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/* 1153 */     byte[] serialdata = null;
/* 1154 */     if (this.serialflag) {
/*      */       
/* 1156 */       this.out.write(data, 6, 8);
/* 1157 */       long dateTimeSend = DateTime.getDateTimeTicks();
/* 1158 */       byte receivedUnitIdentifier = -1;
/* 1159 */       int len = -1;
/* 1160 */       byte[] serialBuffer = new byte[256];
/* 1161 */       serialdata = new byte[256];
/* 1162 */       int expectedlength = 8;
/* 1163 */       int currentLength = 0; while (true) {
/* 1164 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/* 1166 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/* 1168 */           len = -1; do {
/*      */           
/* 1170 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/* 1172 */           for (int i = 0; i < len; i++) {
/*      */             
/* 1174 */             serialdata[currentLength] = serialBuffer[i];
/* 1175 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/* 1179 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/* 1181 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/* 1183 */         data = new byte[256];
/*      */       }
/*      */     } 
/* 1186 */     if (serialdata != null) {
/*      */       
/* 1188 */       data = new byte[262];
/* 1189 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/* 1191 */     if (this.tcpClientSocket.isConnected() | this.udpFlag)
/*      */     {
/* 1193 */       if (this.udpFlag) {
/*      */         
/* 1195 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/* 1196 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/* 1197 */         DatagramSocket clientSocket = new DatagramSocket();
/* 1198 */         clientSocket.setSoTimeout(500);
/* 1199 */         clientSocket.send(sendPacket);
/* 1200 */         data = new byte[2100];
/* 1201 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/* 1202 */         clientSocket.receive(receivePacket);
/* 1203 */         clientSocket.close();
/* 1204 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/* 1208 */         this.outStream.write(data, 0, data.length - 2);
/* 1209 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/* 1211 */           this.sendData = new byte[data.length - 2];
/* 1212 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/* 1213 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/* 1214 */             hl.SendDataChanged(); 
/*      */         } 
/* 1216 */         data = new byte[2100];
/* 1217 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/* 1218 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/* 1220 */           this.receiveData = new byte[numberOfBytes];
/* 1221 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/* 1222 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/* 1223 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/* 1227 */     if (((((data[7] & 0xFF) == 133) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/* 1228 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/* 1229 */     if (((((data[7] & 0xFF) == 133) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/* 1230 */       throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid"); 
/* 1231 */     if (((((data[7] & 0xFF) == 133) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/* 1232 */       throw new QuantityInvalidException("quantity invalid"); 
/* 1233 */     if (((((data[7] & 0xFF) == 133) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0) {
/* 1234 */       throw new ModbusException("error reading");
/*      */     }
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
/*      */   public void WriteSingleRegister(int startingAddress, int value) throws ModbusException, UnknownHostException, SocketException, IOException {
/* 1248 */     if ((((this.tcpClientSocket == null) ? 1 : 0) & (this.udpFlag ? 0 : 1)) != 0)
/* 1249 */       throw new ConnectionException("connection error"); 
/* 1250 */     byte[] registerValue = new byte[2];
/* 1251 */     this.transactionIdentifier = toByteArray(1);
/* 1252 */     this.protocolIdentifier = toByteArray(0);
/* 1253 */     this.length = toByteArray(6);
/* 1254 */     this.functionCode = 6;
/* 1255 */     this.startingAddress = toByteArray(startingAddress);
/* 1256 */     registerValue = toByteArray((short)value);
/*      */     
/* 1258 */     byte[] data = { this.transactionIdentifier[1], 
/* 1259 */         this.transactionIdentifier[0], 
/* 1260 */         this.protocolIdentifier[1], 
/* 1261 */         this.protocolIdentifier[0], 
/* 1262 */         this.length[1], 
/* 1263 */         this.length[0], 
/* 1264 */         this.unitIdentifier, 
/* 1265 */         this.functionCode, 
/* 1266 */         this.startingAddress[1], 
/* 1267 */         this.startingAddress[0], 
/* 1268 */         registerValue[1], 
/* 1269 */         registerValue[0], 
/* 1270 */         this.crc[0], 
/* 1271 */         this.crc[1] };
/*      */     
/* 1273 */     if (this.serialflag) {
/*      */       
/* 1275 */       this.crc = calculateCRC(data, 6, 6);
/* 1276 */       data[data.length - 2] = this.crc[0];
/* 1277 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/* 1279 */     byte[] serialdata = null;
/* 1280 */     if (this.serialflag) {
/*      */       
/* 1282 */       this.out.write(data, 6, 8);
/* 1283 */       long dateTimeSend = DateTime.getDateTimeTicks();
/* 1284 */       byte receivedUnitIdentifier = -1;
/* 1285 */       int len = -1;
/* 1286 */       byte[] serialBuffer = new byte[256];
/* 1287 */       serialdata = new byte[256];
/* 1288 */       int expectedlength = 8;
/* 1289 */       int currentLength = 0; while (true) {
/* 1290 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/* 1292 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/* 1294 */           len = -1; do {
/*      */           
/* 1296 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/* 1298 */           for (int i = 0; i < len; i++) {
/*      */             
/* 1300 */             serialdata[currentLength] = serialBuffer[i];
/* 1301 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/* 1305 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/* 1307 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/* 1309 */         data = new byte[256];
/*      */       }
/*      */     } 
/* 1312 */     if (serialdata != null) {
/*      */       
/* 1314 */       data = new byte[262];
/* 1315 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/* 1317 */     if (this.tcpClientSocket.isConnected() | this.udpFlag)
/*      */     {
/* 1319 */       if (this.udpFlag) {
/*      */         
/* 1321 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/* 1322 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/* 1323 */         DatagramSocket clientSocket = new DatagramSocket();
/* 1324 */         clientSocket.setSoTimeout(500);
/* 1325 */         clientSocket.send(sendPacket);
/* 1326 */         data = new byte[2100];
/* 1327 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/* 1328 */         clientSocket.receive(receivePacket);
/* 1329 */         clientSocket.close();
/* 1330 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/* 1334 */         this.outStream.write(data, 0, data.length - 2);
/* 1335 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/* 1337 */           this.sendData = new byte[data.length - 2];
/* 1338 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/* 1339 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/* 1340 */             hl.SendDataChanged(); 
/*      */         } 
/* 1342 */         data = new byte[2100];
/* 1343 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/* 1344 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/* 1346 */           this.receiveData = new byte[numberOfBytes];
/* 1347 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/* 1348 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/* 1349 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/* 1353 */     if (((((data[7] & 0xFF) == 134) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/* 1354 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/* 1355 */     if (((((data[7] & 0xFF) == 134) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/* 1356 */       throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid"); 
/* 1357 */     if (((((data[7] & 0xFF) == 134) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/* 1358 */       throw new QuantityInvalidException("quantity invalid"); 
/* 1359 */     if (((((data[7] & 0xFF) == 134) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0) {
/* 1360 */       throw new ModbusException("error reading");
/*      */     }
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
/*      */   public void WriteMultipleCoils(int startingAddress, boolean[] values) throws ModbusException, UnknownHostException, SocketException, IOException {
/* 1374 */     byte byteCount = (byte)(values.length / 8 + 1);
/* 1375 */     if (values.length % 8 == 0)
/* 1376 */       byteCount = (byte)(byteCount - 1); 
/* 1377 */     byte[] quantityOfOutputs = toByteArray(values.length);
/* 1378 */     byte singleCoilValue = 0;
/* 1379 */     if ((((this.tcpClientSocket == null) ? 1 : 0) & (this.udpFlag ? 0 : 1)) != 0)
/* 1380 */       throw new ConnectionException("connection error"); 
/* 1381 */     this.transactionIdentifier = toByteArray(1);
/* 1382 */     this.protocolIdentifier = toByteArray(0);
/* 1383 */     this.length = toByteArray(7 + values.length / 8 + 1);
/* 1384 */     this.functionCode = 15;
/* 1385 */     this.startingAddress = toByteArray(startingAddress);
/*      */     
/* 1387 */     byte[] data = new byte[16 + byteCount - 1];
/* 1388 */     data[0] = this.transactionIdentifier[1];
/* 1389 */     data[1] = this.transactionIdentifier[0];
/* 1390 */     data[2] = this.protocolIdentifier[1];
/* 1391 */     data[3] = this.protocolIdentifier[0];
/* 1392 */     data[4] = this.length[1];
/* 1393 */     data[5] = this.length[0];
/* 1394 */     data[6] = this.unitIdentifier;
/* 1395 */     data[7] = this.functionCode;
/* 1396 */     data[8] = this.startingAddress[1];
/* 1397 */     data[9] = this.startingAddress[0];
/* 1398 */     data[10] = quantityOfOutputs[1];
/* 1399 */     data[11] = quantityOfOutputs[0];
/* 1400 */     data[12] = byteCount;
/* 1401 */     for (int i = 0; i < values.length; i++) {
/*      */       byte CoilValue;
/* 1403 */       if (i % 8 == 0) {
/* 1404 */         singleCoilValue = 0;
/*      */       }
/* 1406 */       if (values[i]) {
/* 1407 */         CoilValue = 1;
/*      */       } else {
/* 1409 */         CoilValue = 0;
/*      */       } 
/*      */       
/* 1412 */       singleCoilValue = (byte)(CoilValue << i % 8 | singleCoilValue);
/*      */       
/* 1414 */       data[13 + i / 8] = singleCoilValue;
/*      */     } 
/* 1416 */     if (this.serialflag) {
/*      */       
/* 1418 */       this.crc = calculateCRC(data, data.length - 8, 6);
/* 1419 */       data[data.length - 2] = this.crc[0];
/* 1420 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/* 1422 */     byte[] serialdata = null;
/* 1423 */     if (this.serialflag) {
/*      */       
/* 1425 */       this.out.write(data, 6, 9 + byteCount);
/* 1426 */       long dateTimeSend = DateTime.getDateTimeTicks();
/* 1427 */       byte receivedUnitIdentifier = -1;
/* 1428 */       int len = -1;
/* 1429 */       byte[] serialBuffer = new byte[256];
/* 1430 */       serialdata = new byte[256];
/* 1431 */       int expectedlength = 8;
/* 1432 */       int currentLength = 0; while (true) {
/* 1433 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/* 1435 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/* 1437 */           len = -1; do {
/*      */           
/* 1439 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/* 1441 */           for (int j = 0; j < len; j++) {
/*      */             
/* 1443 */             serialdata[currentLength] = serialBuffer[j];
/* 1444 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */ 
/*      */         
/* 1449 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/* 1451 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/* 1453 */         data = new byte[256];
/*      */       }
/*      */     } 
/* 1456 */     if (serialdata != null) {
/*      */       
/* 1458 */       data = new byte[262];
/* 1459 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/* 1461 */     if (this.tcpClientSocket.isConnected() | this.udpFlag)
/*      */     {
/* 1463 */       if (this.udpFlag) {
/*      */         
/* 1465 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/* 1466 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/* 1467 */         DatagramSocket clientSocket = new DatagramSocket();
/* 1468 */         clientSocket.setSoTimeout(500);
/* 1469 */         clientSocket.send(sendPacket);
/* 1470 */         data = new byte[2100];
/* 1471 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/* 1472 */         clientSocket.receive(receivePacket);
/* 1473 */         clientSocket.close();
/* 1474 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/* 1478 */         this.outStream.write(data, 0, data.length - 2);
/* 1479 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/* 1481 */           this.sendData = new byte[data.length - 2];
/* 1482 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/* 1483 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/* 1484 */             hl.SendDataChanged(); 
/*      */         } 
/* 1486 */         data = new byte[2100];
/* 1487 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/* 1488 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/* 1490 */           this.receiveData = new byte[numberOfBytes];
/* 1491 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/* 1492 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/* 1493 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/* 1497 */     if (((((data[7] & 0xFF) == 143) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/* 1498 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/* 1499 */     if (((((data[7] & 0xFF) == 143) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/* 1500 */       throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid"); 
/* 1501 */     if (((((data[7] & 0xFF) == 143) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/* 1502 */       throw new QuantityInvalidException("quantity invalid"); 
/* 1503 */     if (((((data[7] & 0xFF) == 143) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0) {
/* 1504 */       throw new ModbusException("error reading");
/*      */     }
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
/*      */   public void WriteMultipleRegisters(int startingAddress, int[] values) throws ModbusException, UnknownHostException, SocketException, IOException {
/* 1518 */     byte byteCount = (byte)(values.length * 2);
/* 1519 */     byte[] quantityOfOutputs = toByteArray(values.length);
/* 1520 */     if ((((this.tcpClientSocket == null) ? 1 : 0) & (this.udpFlag ? 0 : 1)) != 0)
/* 1521 */       throw new ConnectionException("connection error"); 
/* 1522 */     this.transactionIdentifier = toByteArray(1);
/* 1523 */     this.protocolIdentifier = toByteArray(0);
/* 1524 */     this.length = toByteArray(7 + values.length * 2);
/* 1525 */     this.functionCode = 16;
/* 1526 */     this.startingAddress = toByteArray(startingAddress);
/*      */     
/* 1528 */     byte[] data = new byte[15 + values.length * 2];
/* 1529 */     data[0] = this.transactionIdentifier[1];
/* 1530 */     data[1] = this.transactionIdentifier[0];
/* 1531 */     data[2] = this.protocolIdentifier[1];
/* 1532 */     data[3] = this.protocolIdentifier[0];
/* 1533 */     data[4] = this.length[1];
/* 1534 */     data[5] = this.length[0];
/* 1535 */     data[6] = this.unitIdentifier;
/* 1536 */     data[7] = this.functionCode;
/* 1537 */     data[8] = this.startingAddress[1];
/* 1538 */     data[9] = this.startingAddress[0];
/* 1539 */     data[10] = quantityOfOutputs[1];
/* 1540 */     data[11] = quantityOfOutputs[0];
/* 1541 */     data[12] = byteCount;
/* 1542 */     for (int i = 0; i < values.length; i++) {
/*      */       
/* 1544 */       byte[] singleRegisterValue = toByteArray(values[i]);
/* 1545 */       data[13 + i * 2] = singleRegisterValue[1];
/* 1546 */       data[14 + i * 2] = singleRegisterValue[0];
/*      */     } 
/* 1548 */     if (this.serialflag) {
/*      */       
/* 1550 */       this.crc = calculateCRC(data, data.length - 8, 6);
/* 1551 */       data[data.length - 2] = this.crc[0];
/* 1552 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/* 1554 */     byte[] serialdata = null;
/* 1555 */     if (this.serialflag) {
/*      */       
/* 1557 */       this.out.write(data, 6, 9 + byteCount);
/* 1558 */       long dateTimeSend = DateTime.getDateTimeTicks();
/* 1559 */       byte receivedUnitIdentifier = -1;
/* 1560 */       int len = -1;
/* 1561 */       byte[] serialBuffer = new byte[256];
/* 1562 */       serialdata = new byte[256];
/* 1563 */       int expectedlength = 8;
/* 1564 */       int currentLength = 0; while (true) {
/* 1565 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/* 1567 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/* 1569 */           len = -1; do {
/*      */           
/* 1571 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/* 1573 */           for (int j = 0; j < len; j++) {
/*      */             
/* 1575 */             serialdata[currentLength] = serialBuffer[j];
/* 1576 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/* 1580 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/* 1582 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/* 1584 */         data = new byte[256];
/*      */       }
/*      */     } 
/* 1587 */     if (serialdata != null) {
/*      */       
/* 1589 */       data = new byte[262];
/* 1590 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/* 1592 */     if (this.tcpClientSocket.isConnected() | this.udpFlag)
/*      */     {
/* 1594 */       if (this.udpFlag) {
/*      */         
/* 1596 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/* 1597 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/* 1598 */         DatagramSocket clientSocket = new DatagramSocket();
/* 1599 */         clientSocket.setSoTimeout(500);
/* 1600 */         clientSocket.send(sendPacket);
/* 1601 */         data = new byte[2100];
/* 1602 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/* 1603 */         clientSocket.receive(receivePacket);
/* 1604 */         clientSocket.close();
/* 1605 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/* 1609 */         this.outStream.write(data, 0, data.length - 2);
/* 1610 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/* 1612 */           this.sendData = new byte[data.length - 2];
/* 1613 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/* 1614 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/* 1615 */             hl.SendDataChanged(); 
/*      */         } 
/* 1617 */         data = new byte[2100];
/* 1618 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/* 1619 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/* 1621 */           this.receiveData = new byte[numberOfBytes];
/* 1622 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/* 1623 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/* 1624 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/* 1628 */     if (((((data[7] & 0xFF) == 144) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/* 1629 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/* 1630 */     if (((((data[7] & 0xFF) == 144) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/* 1631 */       throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid"); 
/* 1632 */     if (((((data[7] & 0xFF) == 144) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/* 1633 */       throw new QuantityInvalidException("quantity invalid"); 
/* 1634 */     if (((((data[7] & 0xFF) == 144) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0) {
/* 1635 */       throw new ModbusException("error reading");
/*      */     }
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
/*      */   public int[] ReadWriteMultipleRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite, int[] values) throws ModbusException, UnknownHostException, SocketException, IOException {
/* 1652 */     byte[] startingAddressReadLocal = new byte[2];
/* 1653 */     byte[] quantityReadLocal = new byte[2];
/* 1654 */     byte[] startingAddressWriteLocal = new byte[2];
/* 1655 */     byte[] quantityWriteLocal = new byte[2];
/* 1656 */     byte writeByteCountLocal = 0;
/* 1657 */     if ((((this.tcpClientSocket == null) ? 1 : 0) & (this.udpFlag ? 0 : 1)) != 0)
/* 1658 */       throw new ConnectionException("connection error"); 
/* 1659 */     if ((((startingAddressRead > 65535) ? 1 : 0) | ((quantityRead > 125) ? 1 : 0) | ((startingAddressWrite > 65535) ? 1 : 0) | ((values.length > 121) ? 1 : 0)) != 0) {
/* 1660 */       throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 125");
/*      */     }
/* 1662 */     this.transactionIdentifier = toByteArray(1);
/* 1663 */     this.protocolIdentifier = toByteArray(0);
/* 1664 */     this.length = toByteArray(6);
/* 1665 */     this.functionCode = 23;
/* 1666 */     startingAddressReadLocal = toByteArray(startingAddressRead);
/* 1667 */     quantityReadLocal = toByteArray(quantityRead);
/* 1668 */     startingAddressWriteLocal = toByteArray(startingAddressWrite);
/* 1669 */     quantityWriteLocal = toByteArray(values.length);
/* 1670 */     writeByteCountLocal = (byte)(values.length * 2);
/* 1671 */     byte[] data = new byte[19 + values.length * 2];
/* 1672 */     data[0] = this.transactionIdentifier[1];
/* 1673 */     data[1] = this.transactionIdentifier[0];
/* 1674 */     data[2] = this.protocolIdentifier[1];
/* 1675 */     data[3] = this.protocolIdentifier[0];
/* 1676 */     data[4] = this.length[1];
/* 1677 */     data[5] = this.length[0];
/* 1678 */     data[6] = this.unitIdentifier;
/* 1679 */     data[7] = this.functionCode;
/* 1680 */     data[8] = startingAddressReadLocal[1];
/* 1681 */     data[9] = startingAddressReadLocal[0];
/* 1682 */     data[10] = quantityReadLocal[1];
/* 1683 */     data[11] = quantityReadLocal[0];
/* 1684 */     data[12] = startingAddressWriteLocal[1];
/* 1685 */     data[13] = startingAddressWriteLocal[0];
/* 1686 */     data[14] = quantityWriteLocal[1];
/* 1687 */     data[15] = quantityWriteLocal[0];
/* 1688 */     data[16] = writeByteCountLocal;
/*      */     
/* 1690 */     for (int i = 0; i < values.length; i++) {
/*      */       
/* 1692 */       byte[] singleRegisterValue = toByteArray(values[i]);
/* 1693 */       data[17 + i * 2] = singleRegisterValue[1];
/* 1694 */       data[18 + i * 2] = singleRegisterValue[0];
/*      */     } 
/* 1696 */     if (this.serialflag) {
/*      */       
/* 1698 */       this.crc = calculateCRC(data, data.length - 8, 6);
/* 1699 */       data[data.length - 2] = this.crc[0];
/* 1700 */       data[data.length - 1] = this.crc[1];
/*      */     } 
/* 1702 */     byte[] serialdata = null;
/* 1703 */     if (this.serialflag) {
/*      */       
/* 1705 */       this.out.write(data, 6, 13 + writeByteCountLocal);
/* 1706 */       long dateTimeSend = DateTime.getDateTimeTicks();
/* 1707 */       byte receivedUnitIdentifier = -1;
/* 1708 */       int len = -1;
/* 1709 */       byte[] serialBuffer = new byte[256];
/* 1710 */       serialdata = new byte[256];
/* 1711 */       int expectedlength = 5 + quantityRead;
/* 1712 */       int currentLength = 0; while (true) {
/* 1713 */         if ((((receivedUnitIdentifier != this.unitIdentifier) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */           break;  while (true) {
/* 1715 */           if ((((currentLength < expectedlength) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) == 0)
/*      */             break; 
/* 1717 */           len = -1; do {
/*      */           
/* 1719 */           } while (((((len = this.in.read(serialBuffer)) <= 0) ? 1 : 0) & ((DateTime.getDateTimeTicks() - dateTimeSend > (10000 * this.connectTimeout)) ? 0 : 1)) != 0);
/*      */           
/* 1721 */           for (int k = 0; k < len; k++) {
/*      */             
/* 1723 */             serialdata[currentLength] = serialBuffer[k];
/* 1724 */             currentLength++;
/*      */           } 
/*      */         } 
/*      */         
/* 1728 */         receivedUnitIdentifier = serialdata[0];
/*      */       } 
/* 1730 */       if (receivedUnitIdentifier != this.unitIdentifier)
/*      */       {
/* 1732 */         data = new byte[256];
/*      */       }
/*      */     } 
/* 1735 */     if (serialdata != null) {
/*      */       
/* 1737 */       data = new byte[262];
/* 1738 */       System.arraycopy(serialdata, 0, data, 6, serialdata.length);
/*      */     } 
/* 1740 */     if (this.tcpClientSocket.isConnected() | this.udpFlag)
/*      */     {
/* 1742 */       if (this.udpFlag) {
/*      */         
/* 1744 */         InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
/* 1745 */         DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
/* 1746 */         DatagramSocket clientSocket = new DatagramSocket();
/* 1747 */         clientSocket.setSoTimeout(500);
/* 1748 */         clientSocket.send(sendPacket);
/* 1749 */         data = new byte[2100];
/* 1750 */         DatagramPacket receivePacket = new DatagramPacket(data, data.length);
/* 1751 */         clientSocket.receive(receivePacket);
/* 1752 */         clientSocket.close();
/* 1753 */         data = receivePacket.getData();
/*      */       }
/*      */       else {
/*      */         
/* 1757 */         this.outStream.write(data, 0, data.length - 2);
/* 1758 */         if (this.sendDataChangedListener.size() > 0) {
/*      */           
/* 1760 */           this.sendData = new byte[data.length - 2];
/* 1761 */           System.arraycopy(data, 0, this.sendData, 0, data.length - 2);
/* 1762 */           for (SendDataChangedListener hl : this.sendDataChangedListener)
/* 1763 */             hl.SendDataChanged(); 
/*      */         } 
/* 1765 */         data = new byte[2100];
/* 1766 */         int numberOfBytes = this.inStream.read(data, 0, data.length);
/* 1767 */         if (this.receiveDataChangedListener.size() > 0) {
/*      */           
/* 1769 */           this.receiveData = new byte[numberOfBytes];
/* 1770 */           System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);
/* 1771 */           for (ReceiveDataChangedListener hl : this.receiveDataChangedListener)
/* 1772 */             hl.ReceiveDataChanged(); 
/*      */         } 
/*      */       } 
/*      */     }
/* 1776 */     if (((((data[7] & 0xFF) == 151) ? 1 : 0) & ((data[8] == 1) ? 1 : 0)) != 0)
/* 1777 */       throw new FunctionCodeNotSupportedException("Function code not supported by master"); 
/* 1778 */     if (((((data[7] & 0xFF) == 151) ? 1 : 0) & ((data[8] == 2) ? 1 : 0)) != 0)
/* 1779 */       throw new StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid"); 
/* 1780 */     if (((((data[7] & 0xFF) == 151) ? 1 : 0) & ((data[8] == 3) ? 1 : 0)) != 0)
/* 1781 */       throw new QuantityInvalidException("quantity invalid"); 
/* 1782 */     if (((((data[7] & 0xFF) == 151) ? 1 : 0) & ((data[8] == 4) ? 1 : 0)) != 0)
/* 1783 */       throw new ModbusException("error reading"); 
/* 1784 */     int[] response = new int[quantityRead];
/* 1785 */     for (int j = 0; j < quantityRead; j++) {
/*      */ 
/*      */ 
/*      */       
/* 1789 */       byte highByte = data[9 + j * 2];
/* 1790 */       byte lowByte = data[9 + j * 2 + 1];
/*      */       
/* 1792 */       byte[] bytes = { highByte, lowByte };
/*      */ 
/*      */       
/* 1795 */       ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
/* 1796 */       response[j] = byteBuffer.getShort();
/*      */     } 
/* 1798 */     return response;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void Disconnect() throws IOException {
/* 1807 */     if (!this.serialflag) {
/*      */       
/* 1809 */       if (this.inStream != null)
/* 1810 */         this.inStream.close(); 
/* 1811 */       if (this.outStream != null)
/* 1812 */         this.outStream.close(); 
/* 1813 */       if (this.tcpClientSocket != null)
/* 1814 */         this.tcpClientSocket.close(); 
/* 1815 */       this.tcpClientSocket = null;
/*      */ 
/*      */     
/*      */     }

/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] toByteArray(int value) {
/* 1827 */     byte[] result = new byte[2];
/* 1828 */     result[1] = (byte)(value >> 8);
/* 1829 */     result[0] = (byte)value;
/* 1830 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte[] toByteArrayDouble(int value) {
/* 1835 */     return ByteBuffer.allocate(4).putInt(value).array();
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte[] toByteArray(float value) {
/* 1840 */     return ByteBuffer.allocate(4).putFloat(value).array();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isConnected() {
/*      */     
/* 1859 */     boolean returnValue = false;
/* 1860 */     if (this.tcpClientSocket == null) {
/* 1861 */       returnValue = false;
/*      */     
/*      */     }
/* 1864 */     else if (this.tcpClientSocket.isConnected()) {
/* 1865 */       returnValue = true;
/*      */     } else {
/* 1867 */       returnValue = false;
/*      */     } 
/* 1869 */     return returnValue;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String getipAddress() {
/* 1878 */     return this.ipAddress;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setipAddress(String ipAddress) {
/* 1887 */     this.ipAddress = ipAddress;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int getPort() {
/* 1896 */     return this.port;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setPort(int port) {
/* 1905 */     this.port = port;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean getUDPFlag() {
/* 1914 */     return this.udpFlag;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setUDPFlag(boolean udpFlag) {
/* 1923 */     this.udpFlag = udpFlag;
/*      */   }
/*      */ 
/*      */   
/*      */   public int getConnectionTimeout() {
/* 1928 */     return this.connectTimeout;
/*      */   }
/*      */   
/*      */   public void setConnectionTimeout(int connectionTimeout) {
/* 1932 */     this.connectTimeout = connectionTimeout;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSerialFlag(boolean serialflag) {
/* 1937 */     this.serialflag = serialflag;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean getSerialFlag() {
/* 1942 */     return this.serialflag;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUnitIdentifier(byte unitIdentifier) {
/* 1947 */     this.unitIdentifier = unitIdentifier;
/*      */   }
/*      */ 
/*      */   
/*      */   public byte getUnitIdentifier() {
/* 1952 */     return this.unitIdentifier;
/*      */   }
/*      */ 
/*      */   
/*      */   public void addReveiveDataChangedListener(ReceiveDataChangedListener toAdd) {
/* 1957 */     this.receiveDataChangedListener.add(toAdd);
/*      */   }
/*      */   
/*      */   public void addSendDataChangedListener(SendDataChangedListener toAdd) {
/* 1961 */     this.sendDataChangedListener.add(toAdd);
/*      */   }
/*      */ }


/* Location:              d:\libs\EasyModbusJava.jar!\de\re\easymodbus\modbusclient\ModbusClient.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */