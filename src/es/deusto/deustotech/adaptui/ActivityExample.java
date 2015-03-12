package es.deusto.deustotech.adaptui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;


@SuppressLint("SdCardPath")
public class ActivityExample extends Activity {

	private GridLayout layout;
	private ProgressDialog progressDialog;
	private AdaptUI adaptUI;
	/**private static final String ONTOLOGY_FILE = "test.owl";
	private static final String ONT_PATH = "storage/emulated/0/Download/";
	private static final String ADAPTUI_NAMESPACE = "http://www.morelab.deusto.es/ontologies/test.owl#";*/
	private static final String ADAPTUI_NAMESPACE = "http://www.example.com/pizza.owl#";
	
	private Timer timer;
	private float draw;
	private float drained,timeElapsed;
	private float Reasonerdrained;
	private float OntologyLoaderDrained;
	private String ontologyName,queryName;
	private long startCountingTime;
	private long stopCountingTime;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_activity);
		
		progressDialog = new ProgressDialog(this); 
		// spinner (wheel) style dialog
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		// better yet - use a string resource getString(R.string.your_message)
		progressDialog.setMessage("Reading Dataset"); 
		progressDialog.setCanceledOnTouchOutside(false);
		// display dialog
		progressDialog.show(); 
		 
		Intent myIntent = getIntent(); // gets the previously created intent
		ontologyName = myIntent.getStringExtra("ontologyName"); // will return "ontologyName"
		queryName = myIntent.getStringExtra("queryName"); // will return "queryName"

		// start async task
		new MyAsyncTaskClass().execute();  
		
		
				
	}
	
	
	private class MyAsyncTaskClass extends AsyncTask<Void, Void, Void> {
		 
        @Override
        protected Void doInBackground(Void... params) {
        	layout = (GridLayout) findViewById(R.id.layout);
    		Collection<View> views = new ArrayList<View>();
    		views.add(layout);
    		
    		// Initializing the framework
    		adaptUI = new AdaptUI(ADAPTUI_NAMESPACE, views);
    				
    				// String	ontology	= "file:storage/emulated/0/Download/University00.owl";
    				/** String[]	queries		= new String[] {
    													// One of the original LUBM queries
    						"file:storage/emulated/0/Download/a.sparql"
    						
    						};*/

    				executeQueries();
    	            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            // put here everything that needs to be done after your async task finishes
            progressDialog.dismiss();
            stop();
            finishWithResult();
            finish();
        }
}
public void executeQueries() {
		
	OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

	//OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
	//String inputFileName="univ-bench.owl";
	File file = new File("storage/emulated/0/Download/" +ontologyName);

	InputStream in = null;
	try {
		in = new FileInputStream(file);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	start();//Starts timer that calculates the mAh drained
	startCountingTime= System.currentTimeMillis();

	model.read(in, null);
	String q1 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select * "+
			"where {?X rdf:type ub:GraduateStudent . "+
			"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";
	
	
	String q2 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select *"+
			"where { ?X rdf:type ub:Student ."+
			"?Y rdf:type ub:Faculty ."+
			 "?Z rdf:type ub:Course ."+
			 "?X ub:advisor ?Y ."+
			 "?Y ub:teacherOf ?Z ."+
			 "?X ub:takesCourse ?Z"+
			 "}";			
	String q3 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select *"+
			"where {"
			+ "?X rdf:type ub:Student"
			+ "}";


		
	String[]	queries		= null;
	 
     if(queryName.equals("Query1")){
     	queries = new String[] {q1};
     }
     if(queryName.equals("Query2")){
      	queries = new String[] {q2};
     }
     if(queryName.equals("Query3")){
      	queries = new String[] {q3};
     }
			/**String queryString = 
			
			"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select * "+
			"where {?X rdf:type ub:GraduateStudent . "+
			"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";*/
	//boolean to measure the ontology loader power consumption within the loop		
	boolean NOTmeasured = true;
	float PrewReasonerDrained = 0;
	for(int i= 0; i<queries.length; i++){
		
		String queryString = queries[i];
		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		
		if(NOTmeasured){
			//records how much loader drained of a battery
			OntologyLoaderDrained = drained;
			write("ontLoader", OntologyLoaderDrained +"\n");
			NOTmeasured = false;
		}
		
		com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
		
		//converts results to the string
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ResultSetFormatter.out(ps, results, query) ;
		String s = "";
		try {
			 s = new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(s);
		
		//records how much reasoner drained.
		Reasonerdrained = drained - OntologyLoaderDrained- PrewReasonerDrained;
		
		stopCountingTime = System.currentTimeMillis()-startCountingTime;	
		float timeElapsed2 = stopCountingTime;
		float timeElapsed = timeElapsed2/1000;		//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
		write("Times", "Pellet loader :" +timeElapsed + "s");
		startCountingTime= System.currentTimeMillis();
		
		
		//keeps record of previous reasoner
		PrewReasonerDrained = PrewReasonerDrained + Reasonerdrained;
		System.out.println("There was " + OntologyLoaderDrained + "mAh" + " drained by ontology loader");
		System.out.println("There was " + Reasonerdrained + "mAh" + " drained by reasoner");
		
		
		write("log", "________________________________________\n"+"Query: "+ queryName + "\n"+"Pellet Reasoner " +Reasonerdrained+"mAh"+"\n"
		+ "Pellet ont loader " + OntologyLoaderDrained +"mAh"+"\n" + "Pellet Total drained "+drained +"mAh"+"\n"
		+"Pellet Running : " + ontologyName+"\n________________________");
		write("justdata", "\n"+Reasonerdrained +"\n");
		write("Results", "\n"+s +"\n");

		
		qe.close();
	}
	
	write("ontLoader", "\n");
	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	float timeElapsed2 = stopCountingTime;
	float timeElapsed = timeElapsed2/1000;	//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
	write("Times", "Pellet Reasoner :" +timeElapsed + "s");
	//finish();
	
	
	
		
	}

public  float bat(){
			BatteryManager mBatteryManager =
					(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
					Long energy =
					mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);					
			float currentdraw = energy;
			draw = currentdraw;		

			return draw;
}


public void start() {
    if(timer != null) {
        return;
    }
    timer = new Timer();	   
    timer.schedule(new TimerTask() {
        public void run() {	            
        	float curret =bat(); 
        	drained =drained +(curret/64000);
        	runOnUiThread(new Runnable() {

        	    @Override
        	    public void run() {
        	    	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
    				float timeElapsed2 = stopCountingTime;
    				timeElapsed = timeElapsed2/1000;		
	        		((TextView)findViewById(R.id.textView)).setText("Capacity Drained = " + drained + "mAh \n"+
    				"Time Elapsed: "+timeElapsed+"s");
	        		//This if ABORTS the reasoning task because it took too long,
	        		if(timeElapsed>900||drained>60){
	        			write("log", "ABORTED due to Out Of Memory/Time \n"+"________________________________________\n"+"Query: "+ queryName + "\n"+"AndroJena Reasoner " +Reasonerdrained+"mAh"+"\n"
	        		    		+ "Pellet ont loader " + OntologyLoaderDrained +"mAh"+"\n" + "Pellet Total: " +drained+"mAh"+ "\n"
	        		    		+"Pellet Running : " + ontologyName+"\n Time Elapsed: "+timeElapsed+"s"+"\n________________________");
	        		    		write("justdata", "\n"+Reasonerdrained +"\n");
	        		    		write("Results", "\n"+"NO RESULTS " +"\n");

	        		    		stop();
	        		            finishWithResult();
	        		            finish();		   
	        		}
        	            }
        	    });
        	
       }
   }, 0, 50 );
}
public void stop() {
    timer.cancel();
    timer = null;
}




//File writter
public void write(String fname, String fcontent){
    String filename= "storage/emulated/0/Download/"+fname+".txt";
    String temp = read(fname);
    BufferedWriter writer = null;
    try {
        //create a temporary file
        File logFile = new File(filename);

        // This will output the full path where the file will be written to...
        System.out.println(logFile.getCanonicalPath());

        writer = new BufferedWriter(new FileWriter(logFile));
        
        writer.write(temp + fcontent );
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            // Close the writer regardless of what happens...
            writer.close();
        } catch (Exception e) {
        }
    }
}

//File reader
   public String read(String fname){
     BufferedReader br = null;
     String response = null;
      try {
        StringBuffer output = new StringBuffer();
        String fpath = "storage/emulated/0/Download/"+fname+".txt";
        br = new BufferedReader(new FileReader(fpath));
        String line = "";
        while ((line = br.readLine()) != null) {
          output.append(line +"\n");
        }
        response = output.toString();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
      return response;
   }

   private void finishWithResult()
   {
      Bundle conData = new Bundle();
      conData.putInt("results", 1);
      Intent intent = new Intent();
      intent.putExtras(conData);
      setResult(RESULT_OK, intent);
      finish();
   }
   

}