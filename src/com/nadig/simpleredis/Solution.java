/*
 * Created by cnadig on 8/29/16.
 * Email : chidambaranadig@gmail.com
 *
 * Things to improve
 *      1. Replace HashMap with some other datastructure which takes O(log n) for Read and Writes.
 *      2. Verify access modifiers once again.
 *      3. Verify Static and Non-Static objects once again.
 */
package com.nadig.simpleredis;
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

        boolean returnCode=false;
        String[] args = cmd.split("\\s+");

        String command = args[0].toUpperCase();

        switch (command) {
            case "SET": {
                if (args.length != 3) {
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }

                String key = args[1];
                int value = Integer.parseInt(args[2]);
                db.write(key, value);
                break;
            }
            case "GET": {
                if(args.length!=2) {
                    System.out.println("> Incorrect Number of Arguments!");
                    return;
                }

                String key = args[1];
                Integer value = db.read(key);
                if (value == null)
                    System.out.println("> NULL");
                else
                    System.out.println("> " + value.toString());
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

                int value = Integer.parseInt(args[1]);
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
    The in-memory database is implemented using a HashMap
    Reads take O(1)
    Writes take O(1)
     */
    private HashMap<String, Integer> data;

    /*
    A stack is maintained to keep track of nested transactions
     */
    private ArrayList<String> rollbackStatements;

    private boolean transactionOpen;

    private boolean rollbackCommands;

    Database() {
        data = new HashMap<>();
        rollbackStatements = null;
        transactionOpen=false;
        rollbackCommands=false;
    }

    public void attachCli(Cli c){
        this.cli = c;
    }

    public Integer read(String key){
        return data.get(key);
    }
    public void write(String key, int value){

        if(transactionOpen && !rollbackCommands) {

            String rollbackCommand;

            if(!data.containsKey(key)){
                rollbackCommand = "UNSET " + key;
            }
            else{
                rollbackCommand = "SET " + key + " " + data.get(key);
            }
            rollbackStatements.add(rollbackCommand);
        }
        data.put(key, value);
    }

    public void remove(String key){

        if(transactionOpen && !rollbackCommands) {
            if(data.containsKey(key)){
                String rollbackCommand;
                rollbackCommand = "SET " + key + " " + data.get(key);
                rollbackStatements.add(rollbackCommand);
            }
        }
        data.remove(key);
    }

    public int findAllOccurances(int value){
        int count=0;
        for(Map.Entry<String, Integer> e : data.entrySet()){
            if (e.getValue() == value)
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