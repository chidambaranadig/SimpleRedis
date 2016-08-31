/*
 * Created by cnadig on 8/29/16.
 * Email : chidambaranadig@gmail.com
 */
package com.nadig.simpleredis;

import java.util.*;
import java.util.logging.Logger;

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
        String cmd = in.nextLine().trim();
        //boolean validCommand;

        while(!cmd.toUpperCase().equals("END")){

            //Print Command
            System.out.println(cmd);

            evaluate(cmd);

            //Read Next Command to Process
            cmd = in.nextLine().trim();
        }
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
            default: {
                System.out.println("> Command not recognized!");
            }
        }
    }
}
class Database {

    //private Logger logger = Logger.getLogger("com.nadig.simpleredis.Database");

    private Cli cli;

    private HashMap<String, Integer> data;

    private ArrayList<String> transactionStatements;

    private boolean transactionOpen;

    Database() {
        data = new HashMap<>();
        transactionStatements = null;
        transactionOpen=false;
    }

    public void attachCli(Cli c){
        this.cli = c;
    }

    public Integer read(String key){
        return data.get(key);
    }
    public void write(String key, int value){

        if(!transactionOpen) {
            //logger.info("Writing Directly to Database");
            data.put(key, value);
        }
        else {
            //logger.info("Transaction Open. Adding command to transaction statements.");
            String statement = "SET " + key + " " + value;
            transactionStatements.add(statement);
        }
    }

    public void remove(String key){
        if(!transactionOpen)
            data.remove(key);
        else {
            String statement = "UNSET " + key;
            transactionStatements.add(statement);
        }
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

        if(transactionStatements==null){
            transactionStatements=new ArrayList<String>();
        }
        transactionOpen = true;
        transactionStatements.add("BEGIN");

        //logger.info("BEGIN : Started Transaction: "+transactionStatements.size());


    }

    public boolean transactionRollback(){

        boolean rollbackSuccessful=false;

        if(transactionOpen) {

            //logger.info("ROLLBACK: Number of Transaction Statements Before: " + transactionStatements.size());

            int fromIndex = transactionStatements.lastIndexOf("BEGIN");
            // Truncating statements from most recent "BEGIN" to end.
            transactionStatements.subList(fromIndex, transactionStatements.size()).clear();

            //Close Transaction if all statements have been rolled back
            if(transactionStatements.size()==0)
                transactionOpen=false;

            //logger.info("ROLLBACK: Number of Transaction Statements After: " + transactionStatements.size());

            rollbackSuccessful=true;
        }
        return rollbackSuccessful;
    }

    public boolean transactionCommit(){

        boolean commitSuccessful = false;

        if(transactionOpen) {

            transactionOpen = false;

            //logger.info("COMMIT: Number of Transaction Statements Before: " + transactionStatements.size());

            for (Iterator<String> iterator = transactionStatements.iterator(); iterator.hasNext(); ) {
                String cmd = iterator.next();
                if (!cmd.equals("BEGIN")) {
                    //logger.info("Executing Transaction command : " + cmd);
                    cli.evaluate(cmd);
                }

            }

            transactionStatements.clear();
            //logger.info("COMMIT: Number of Transaction Statements After: " + transactionStatements.size());

            commitSuccessful = true;
        }
        return commitSuccessful;
    }
}

