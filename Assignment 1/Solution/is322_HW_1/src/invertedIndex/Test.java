/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 *
 * @author ehab
 */
public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();
        //|**  change it to your collection directory 
        //|**  in windows "C:\\tmp11\\rl\\collection\\"       





        int n;
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("Enter 1=> print dictionary ");
            System.out.println("Enter 2=> print Posting list ");

            System.out.print("Enter number:");
            n = scanner.nextInt();
            if (n == 1) {
                index.printDictionary();
                System.out.println();
            }



        }
    }


}
