/*
 * Created by cnadig on 8/29/16.
 */
package com.nadig.simpleredis;

import java.util.*;

public class Solution {

    public static void main(String args[] ) throws Exception {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */

        //Initialize a new Database
        Database db = new Database();

        Cli redis_cli = new Cli(db);
        redis_cli.startRepl();
    }
}
class Cli{

    private Database db;

    Cli(Database db){
        this.db = db;
    }

    /*private enum Commands {
        SET, GET, UNSET, NUMEQUALTO, END, BEGIN, ROLLBACK, COMMIT;

        private static Set<String> _values = new HashSet<>();

        static {
            for (Commands cmd : Commands.values()) {
                _values.add(cmd.name());
            }
        }
        public static boolean validate(String value) {
            String[] args = value.trim().split("\\s+");
            String command = args[0].toUpperCase();
            return _values.contains(command);
        }
    }*/

    void startRepl(){

        Scanner in = new Scanner(System.in);
        String cmd = in.nextLine().trim();
        //boolean validCommand;

        while(!cmd.toUpperCase().equals("END")){

            //Print Command
            System.out.println(cmd);

            /*
            validCommand = Commands.validate(cmd);

            if(!validCommand)
                System.out.println("invalid command");
            else {
                evaluate(cmd);
            }
            */
            evaluate(cmd);

            //Read Next Command to Process
            cmd = in.nextLine().trim();
        }
    }
    private void evaluate(String cmd){
        String[] args = cmd.split("\\s+");

        String command = args[0].toUpperCase();

        switch (command) {
            case "SET": {
                String key = args[1];
                int value = Integer.parseInt(args[2]);
                db.data.put(key, value);
                break;
            }
            case "GET": {
                String key = args[1];
                Integer value = db.data.get(key);
                if (value == null)
                    System.out.println("> NULL");
                else
                    System.out.println("> " + value.toString());
                break;
            }
            case "UNSET": {
                String key = args[1];
                db.data.remove(key);
                break;
            }
            case "NUMEQUALTO": {
                int count = 0;

                int value = Integer.parseInt(args[1]);

                for (Map.Entry<String, Integer> e : db.data.entrySet()) {
                    if (e.getValue() == value)
                        count++;
                }
                System.out.println("> " + count);
                break;
            }
        }
    }
}
class Database {

    HashMap<String, Integer> data;

    Database() {
        data = new HashMap<>();
    }
}