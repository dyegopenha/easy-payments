package com.example.easy_payments.util;

public class LuhnValidator {

   public static boolean isValid(String number) {
      if (number == null || number.isEmpty()) {
         return false;
      }

      // Remove any non-digit characters (e.g., spaces or hyphens)
      String cleanNumber = number.replaceAll("[^0-9]", "");

      int sum = 0;
      boolean alternate = false;

      for (int i = cleanNumber.length() - 1; i >= 0; i--) {
         int digit = Character.getNumericValue(cleanNumber.charAt(i));

         if (alternate) {
            digit *= 2;
            if (digit > 9) {
               digit = (digit % 10) + 1;
            }
         }
         sum += digit;
         alternate = !alternate;
      }
      return (sum % 10 == 0);
   }
}
