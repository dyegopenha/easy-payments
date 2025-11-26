package com.example.easy_payments.util;

public class MaskUtils {

   public static String maskCard(String card) {
      if (card == null || card.length() < 4) return "****";
      return "************" + card.substring(card.length()-4);
   }
}
