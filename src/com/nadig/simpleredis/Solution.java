/**
 * Created by cnadig on 8/29/16.
 */
package com.nadig.simpleredis;

import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class Solution {

    public static HashMap<String, Integer> database = new HashMap<>();

    public static void main(String args[] ) throws Exception {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */

        Cli redis_cli = new Cli();
        redis_cli.startRepl();
    }
}
class Cli{

    public static enum Commands {
        SET(2), GET(1), UNSET(1), NUMEQUALTO(1), END(0), BEGIN(0), ROLLBACK(0), COMMIT(0);

        private static Set<String> _values = new HashSet<>();

        private final int numberOfArgs;

        Commands(int x){
            numberOfArgs=x;
        }

        static{
            for (Commands cmd : Commands.values()) {
                _values.add(cmd.name());
            }
        }

        public int getNumberOfArgs() {
            return this.numberOfArgs;
        }

        public static boolean validate(String value){

            String[] args = value.split("\\s+");
            String command = args[0].toUpperCase();

            if(_values.contains(command)) {

            }

            return false;
        }
    }


    public void startRepl(){

        Scanner in = new Scanner(System.in);
        String cmd = in.nextLine().trim();
        boolean validCommand = false;

        while(!cmd.toLowerCase().equals(Commands.END)){

            //Print Command
            System.out.println(cmd);

            validCommand = Commands.validate(cmd);

            if(!validCommand)
                System.out.println("invalid command");
            else {

            }

            //Read Next Command to Process
            cmd = in.nextLine().trim();
        }
    }
}