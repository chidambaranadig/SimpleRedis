/*
 * Created by cnadig on 8/29/16.
 * Email : chidambaranadig@gmail.com
 * Github : https://github.com/chidambaranadig
 *
 * Things to improve
 *      1. Verify access modifiers once again.
 *      2. Verify Static and Non-Static objects once again.
 */
//package com.nadig.simpleredis;

import java.util.*;

public class Solution {

    public static void main(String args[] ) throws Exception {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */

        //Initialize a new Database
        Database db = new Database();

        Cli redis_cli = new Cli(db);

        db.attachCli(redis_cli);

        redis_cli.startRepl();
    }
}
class Cli{

    private Database db;

    Cli(Database db){
        this.db = db;
    }

    void startRepl(){

        Scanner in = new Scanner(System.in);
        String cmd;

        do{
            cmd= in.nextLine().trim();
            System.out.println(cmd);

            // Evaluate Command
            evaluate(cmd);

        } while(!cmd.toUpperCase().equals("END"));
    }
    public void evaluate(String cmd){

        boolean returnCode;
        String[] args = cmd.split("\\s+");

        String command = args[0].toUpperCase();

        switch (command) {
            case "SET": {
                if (args.length != 3) {
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }

                String key = args[1];
                String value = args[2];
                db.write(key, value);
                break;
            }
            case "GET": {
                if(args.length!=2) {
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }

                String key = args[1];
                String value = db.read(key);
                if (value == null)
                    System.out.println("> NULL");
                else
                    System.out.println("> " + value);
                break;
            }
            case "UNSET": {
                if(args.length!=2) {
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }
                String key = args[1];
                db.remove(key);
                break;
            }
            case "NUMEQUALTO": {
                if(args.length!=2) {
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }

                String value = args[1];
                int count = db.findAllOccurances(value);

                System.out.println("> " + count);
                break;
            }
            case "BEGIN":{
                if(args.length!=1){
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }
                db.transactionBegin();
                break;
            }
            case "ROLLBACK": {
                if(args.length!=1){
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }
                returnCode=db.transactionRollback();
                if(!returnCode)
                    System.out.println("> NO TRANSACTION");
                break;
            }
            case "COMMIT": {
                if(args.length!=1){
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }
                returnCode=db.transactionCommit();
                if(!returnCode)
                    System.out.println("> NO TRANSACTION");
                break;
            }
            case "":
            case "END": break; // No action for END command here. Do-While loop terminates in cli.startRepl() function

            default: {
                System.out.println("> Command not recognized!");
            }
        }
    }
}
class Database {

    /*
    Associate a command line object for this database.
    The CLI is used to execute rollback commands.
     */
    private Cli cli;

    /*
    The in-memory database is implemented using a TreeMap
    Reads take O(log n)
    Writes take O(log n)
    Updates take O(log n)
    Deletes take O(log n)

    Space Complexity for n Key-Values : O(n)
     */
    private TreeMap<String,String> database;

    /*
    A stack is maintained to keep track of nested transactions
     */
    private ArrayList<String> rollbackStatements;

    private boolean transactionOpen;

    private boolean rollbackCommands;

    Database() {
        database = new TreeMap<String,String>();
        rollbackStatements = null;
        transactionOpen=false;
        rollbackCommands=false;
        cli=null;
    }

    public void attachCli(Cli c){
        this.cli = c;
    }

    public String read(String key){
        return database.get(key);
    }
    public void write(String key, String value){

        if(transactionOpen && !rollbackCommands) {

            String rollbackCommand;

            /*
            The Following If-Else block checks if a key is being newly added
            or if an existing key is being updated.

            A corresponding rollback statement is constructed and added onto the rollBackStatements stack.
             */

            if(!database.containsKey(key)){
                rollbackCommand = "UNSET " + key;
            }
            else{
                rollbackCommand = "SET " + key + " " + database.get(key);
            }
            rollbackStatements.add(rollbackCommand);
        }
        database.put(key, value);
    }

    public void remove(String key){

        if(transactionOpen && !rollbackCommands) {
            if(database.containsKey(key)){
                String rollbackCommand;
                rollbackCommand = "SET " + key + " " + database.get(key);
                rollbackStatements.add(rollbackCommand);
            }
        }
        database.remove(key);
    }

    public int findAllOccurances(String value){
        int count=0;
        for(Map.Entry<String, String> e : database.entrySet()){
            if (e.getValue().equals(value))
                count++;
        }
        return count;
    }

    public void transactionBegin(){

        if(rollbackStatements ==null){
            rollbackStatements =new ArrayList<String>();
        }
        transactionOpen = true;
        rollbackStatements.add("BEGIN");
    }

    public boolean transactionRollback(){

        boolean rollbackSuccessful=false;

        if(transactionOpen){

            int mostRecentBegin = rollbackStatements.lastIndexOf("BEGIN");
            rollbackCommands=true;
            for(int i=mostRecentBegin+1; i<rollbackStatements.size(); i++){
                cli.evaluate(rollbackStatements.get(i));
            }
            rollbackCommands=false;
            rollbackSuccessful=true;

            // Remove rollback statements that were executed from stack
            rollbackStatements.subList(mostRecentBegin,rollbackStatements.size()).clear();

            // Close Transaction if there are no more Rollback statements in the stack
            if(rollbackStatements.size()==0)
                transactionOpen = false;
        }
        return rollbackSuccessful;
    }

    public boolean transactionCommit(){

        boolean commitSuccessful = false;

        if(transactionOpen) {
            rollbackStatements.clear();
            transactionOpen=false;
            commitSuccessful=true;
        }
        return commitSuccessful;
    }
}
