/*    */ package de.re.easymodbus.modbusclient;
/*    */ 
/*    */ import java.text.DateFormat;
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.Calendar;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class DateTime
/*    */ {
/*    */   protected static long getDateTimeTicks() {
/* 18 */     long TICKS_AT_EPOCH = 621355968000000000L;
/* 19 */     long tick = System.currentTimeMillis() * 10000L + TICKS_AT_EPOCH;
/* 20 */     return tick;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected static String getDateTimeString() {
/* 29 */     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
/* 30 */     Calendar cal = Calendar.getInstance();
/* 31 */     return dateFormat.format(cal.getTime());
/*    */   }
/*    */ }


/* Location:              d:\libs\EasyModbusJava.jar!\de\re\easymodbus\modbusclient\DateTime.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */