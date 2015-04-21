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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
	private String datasetFileName, queryName, ontologyName;
	private long startCountingTime;
	private long stopCountingTime;
	
	private BroadcastReceiver batteryInfoReceiver;
	private int mvoltage;
	private float watts;
	private float ReasonerdrainedWatts;
	private float OntologyLoaderDrainedWatts;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    Intent myIntent = getIntent(); // gets the previously created intent
		datasetFileName = myIntent.getStringExtra("ontologyFile"); // will return the name of ontology file
		queryName = myIntent.getStringExtra("queryName"); // will return "queryName"
		ontologyName = myIntent.getStringExtra("ontologyName"); //returns the name of ontology size
		if(datasetFileName==null){
			System.out.println("CLOSED. Dataset Empty");
            
    		Thread thread = new Thread(){
                @Override
               public void run() {
                    try {
                       Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
                        	finish();	
                        	System.exit(0);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                }  
              };
              
              Toast.makeText(getApplicationContext(), "Launch From The PowerBenchMark app", Toast.LENGTH_LONG).show();
              thread.start();

    	}else{
		
		progressDialog = new ProgressDialog(this); 
		// spinner (wheel) style dialog
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		// better yet - use a string resource getString(R.string.your_message)
		progressDialog.setMessage("Please Wait"); 
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(new OnCancelListener() {

	        @Override
	        public void onCancel(DialogInterface dialog) {
	        	onBackPressed();
	        }});
		// display dialog
		progressDialog.show(); 
		 
		
		// start async task

		new MyAsyncTaskClass().execute(); 
		} 
		
		
				
	}
	
	
	private class MyAsyncTaskClass extends AsyncTask<Void, Void, Void> {
		 
        @Override
        protected Void doInBackground(Void... params) {
        	layout = (GridLayout) findViewById(R.id.layout);
    		Collection<View> views = new ArrayList<View>();
    		views.add(layout);
    		
	        	
    		executeQueries();
    		
    	    return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            // put here everything that needs to be done after your async task finishes
            progressDialog.dismiss();
            stop();
            finishWithResult(1);
            finish();
            System.exit(0);
            }
}
public void executeQueries() {
		
	OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

	
	File file = new File("storage/emulated/0/Download/" +datasetFileName);

	InputStream in = null;
	try {
		in = new FileInputStream(file);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	start();//Starts timer that calculates the mAh drained
	getVoltage();
	startCountingTime= System.currentTimeMillis();

	model.read(in, null);
	String q1 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select * "+
			"where {?X rdf:type ub:GraduateStudent . "+
			"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";
	
	
	String q2 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select * "+
			"where {?X rdf:type ub:Student . "+
			"?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>} ";
	
	
	String q3 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "+
			"select *"+
			"where {"
			+ "?X rdf:type ub:Student"
			+ "}";
	String q4 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			"prefix ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> "
			+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
			"select *"+
			"where {"
			//+ "?x rdfs:subClassOf ub:Employee"
			+ "?X rdfs:subClassOf ?Y"
			+ "}";


		
	String[]	queries		= null;
	 
		if(queryName.equals("Instance Retrieval")){
	     	queries = new String[] {q1};
	     }
	     if(queryName.equals("Inference & Instance Retrieval")){
	      	queries = new String[] {q2};
	     }
	     if(queryName.equals("Inference")){
	      	queries = new String[] {q3};
	     }
	     if(queryName.equals("Classification")){
	        	queries = new String[] {q4};
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
	float PrewReasonerDrainedWatts = 0;
	for(int i= 0; i<queries.length; i++){
		try{	
			String queryString = queries[i];
			System.out.println(queryString);
			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			
			if(NOTmeasured){
				//records how much loader drained of a battery
				OntologyLoaderDrained = drained;
   				OntologyLoaderDrainedWatts = watts;	
   				write("ontLoader",""+ OntologyLoaderDrained);
   				write("PowerLoader",""+ OntologyLoaderDrainedWatts);
				NOTmeasured = false;
			}
			
			stopCountingTime = System.currentTimeMillis()-startCountingTime;	
			float timeElapsed2 = stopCountingTime;
			timeElapsed = timeElapsed2/1000;
			write("LoaderTime", "" +timeElapsed);
			startCountingTime= System.currentTimeMillis();
			
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
			
    		//records how much mAh reasoner drained.
			Reasonerdrained = drained - OntologyLoaderDrained- PrewReasonerDrained;
			//records how much watts reasoner drained
			ReasonerdrainedWatts = watts - OntologyLoaderDrainedWatts- PrewReasonerDrainedWatts;

			//keeps record of previous reasoner
			PrewReasonerDrained = PrewReasonerDrained + Reasonerdrained;
			PrewReasonerDrainedWatts = PrewReasonerDrainedWatts + ReasonerdrainedWatts;
			
			
			write("log", "________________________________________\n"
		    		+"PELLET REASONER:\n"
		    		+"Reasoning task: "+ queryName + "  \n"
		    		+ "Ontology size : " + ontologyName+ "\n"
		    		+"Reasoning task drained: " +Reasonerdrained+"mAh"+"\n"
		    		+ "Ontology loader drained: " + OntologyLoaderDrained +"mAh"+"\n" 
		    		+ "Pellet drained total: " +drained+"mAh"+ "\n"
		    		+ "Time elapsed: "+timeElapsed+"s\n"
		    		+ "Power consumed: "+watts+"W"
		    		+"\n________________________");
			write("justdata", ""+Reasonerdrained );
    		write("PowerReasoner", ""+ ReasonerdrainedWatts);
			write("Results", ""+s );
	
			
			qe.close();
		} catch (OutOfMemoryError E) {
			System.err.println(E);
			quiteAnApp(1);
		}
	}
	
	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	float timeElapsed2 = stopCountingTime;
	float timeElapsed = timeElapsed2/1000;	//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
	write("ReasonerTime", "" +timeElapsed );
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
        	final float curret =bat();
        	drained =drained +(curret/3300);//3300s instead 3600s because after calculations there 
        	//were some error rate determined and diviation from 3300 covers the loss of data that
        	//was missed to be recorded. Calculated by measuring amount of current drained per 1% and finding 
        	//the constant that derives 31mah
        	watts = (float) ((drained*mvoltage/1000)*3.6);
        	runOnUiThread(new Runnable() {

        	    @Override
        	    public void run() {
        	    	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
    				float timeElapsed = (float) (stopCountingTime/1000.0);	
    				((TextView)findViewById(R.id.textView)).setText("PELLET REASONER:\n"
							+"Reasoning task: "+ queryName + " \n"
							+ "Ontology size : " + ontologyName+ "\n"
							+"Capacity drained = " + drained + "mAh \n"
							+"Time elapsed : " +timeElapsed + "s"
							+ "\nPower consumed: "+watts+"W");
    				//This if ABORTS the reasoning task because it took too long,
	        		if(timeElapsed>300||drained>45){
	        			quiteAnApp(1);
	        		}
        	    }
        	 });
        	
       }
   }, 0, 1000);
}
public void stop() {
	if(timer!=null){
		timer.cancel();
    }
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

   private void finishWithResult(int a)
   {
      Bundle conData = new Bundle();
      conData.putInt("results", a);
      Intent intent = new Intent();
      intent.putExtras(conData);
      setResult(RESULT_OK, intent);
   }
   public void quiteAnApp(int a){
	   
	   Reasonerdrained = drained-OntologyLoaderDrained;
	   ReasonerdrainedWatts = watts-OntologyLoaderDrainedWatts;
	   stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	   			write("log", "________ABORTED____________\n"
	    		+"PELLET REASONER:\n"
	    		+"Reasoning task: "+ queryName + "\n"
	    		+ "Ontology size : " + ontologyName+ "\n"
	    		+"Reasoning task drained: " +Reasonerdrained+"mAh"+"\n"
	    		+ "Ontology loader drained: " + OntologyLoaderDrained +"mAh"+"\n" 
	    		+ "Pellet drained total: " +drained+"mAh"+ "\n"
	    		+ "Time elapsed: "+timeElapsed+"s\n"
	    		+ "Power consumed: "+watts+"W"
	    		+"\n________________________");
	   			write("justdata", ""+Reasonerdrained );
	    		write("PowerReasoner", ""+ ReasonerdrainedWatts);
	    		write("Results", "Results Aborted " );
				write("ReasonerTime", "" +timeElapsed );
	            progressDialog.dismiss();
	    		stop();
	            finishWithResult(1);
	            finish();
	            System.exit(0);
   }
   public void getVoltage(){
       batteryInfoReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {			
				mvoltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);				
			}
		};
		registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
   /**
    * Method created the pop up dialog asking if user
    * wants really quite and application.
    */
   @Override
   public void onBackPressed() {
   	 
   		final Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.customexit);			
			dialog.setTitle("Pellet");
			// set the custom dialog components - text, image and button
			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText("Are you sure you want");
			TextView text2 = (TextView) dialog.findViewById(R.id.text2);
			text2.setText("to CANCEL reasoning?");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageResource(R.drawable.cancel); 
			Button dialogButton = (Button) dialog.findViewById(R.id.btnok);
			Button dialogButton2 = (Button) dialog.findViewById(R.id.btncancel);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					quiteAnApp(-1);
             		dialog.dismiss();             		
				}
			});
			
			dialogButton2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					progressDialog.show(); 
					dialog.dismiss();
					
				}
			});
			
			dialog.setOnCancelListener(new OnCancelListener() {

		        @Override
		        public void onCancel(DialogInterface dialog) {
		    		progressDialog.show(); 
		        }});
 
			dialog.show();
   }

}
