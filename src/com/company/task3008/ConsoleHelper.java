package com.company.task3008;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.System.out;

/**
 * Created by i.lapshinov on 06.09.2018.
 */
public class ConsoleHelper {
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message)
    {
        out.println(message);
    }

    public static  String readString()
    {
        while(true) {
            try {
                String str = br.readLine();
                return str;
            } catch (IOException e) {
                writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }


    }

    public static int readInt()
    {
        while (true) {
            try {
                int ch = Integer.parseInt(readString());
                return ch;
            } catch (NumberFormatException e)
            {
                writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }
}
