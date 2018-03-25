
package com.lahari.voting;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class VotingActivity extends Activity {
	
	private static AsyncHttpClient client = new AsyncHttpClient();;
	private static final String VOTING_URL = "http://www.ieap.uni-kiel.de/plasma/ag-kersten/vote/vote.php";
	private BarGraph graph;
	private Button a;
	private Button b;
	private Button c;
	private Button d;
	private boolean continueToLoad = true;	
	private Handler handler = new Handler();
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voting);
		
		graph = (BarGraph) findViewById(R.id.bargraph);
		a = (Button) findViewById(R.id.buttonA);
		b = (Button) findViewById(R.id.buttonB);
		c = (Button) findViewById(R.id.buttonC);
		d = (Button) findViewById(R.id.buttonD);

		runnable = new Runnable() {
		    @Override
		    public void run() {
		        if (continueToLoad) {		    		
		    		loadVoting();
		            handler.postDelayed(this, 1000);
		        }
		    }
		};
		dbHelper = new ExampleDBHelper(this);
		a.setOnClickListener(voteOnClickListener("A"));
		b.setOnClickListener(voteOnClickListener("B"));
		c.setOnClickListener(voteOnClickListener("C"));
		d.setOnClickListener(voteOnClickListener("D"));


	}

	private OnClickListener voteOnClickListener(final String vote) {
		return new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				vote(vote);
			}			
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_info:
			Intent intent = new Intent(this, InfoActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void loadVoting() {
		RequestParams params = new RequestParams();
		params.put("action", "getResult");
		client.post(VOTING_URL, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject votingStatus) {
				
				//Add Bars
				ArrayList<Bar> bars = new ArrayList<Bar>();
				Log.d("Voting", votingStatus.toString());
				try {
					bars.add(createBar(votingStatus.getInt("A"), "A"));
					bars.add(createBar(votingStatus.getInt("B"), "B"));
					bars.add(createBar(votingStatus.getInt("C"), "C"));
					bars.add(createBar(votingStatus.getInt("D"), "D"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				graph.setBars(bars);
				
				//Buttons
				try {
					boolean pollOpen = votingStatus.getInt("status") == 1 ? true : false;
					setButtonsEnabled(pollOpen);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private Bar createBar(int value, String name) {
		Bar bar = new Bar();
		bar.setColor(Color.parseColor("#FFBB33"));
		bar.setLabelColor(Color.parseColor("#000000"));
		bar.setValue(value);
		bar.setValueString(String.valueOf(value));
		bar.setName(name);
		return bar;
	}
	
	private void vote(String vote) {
		setButtonsEnabled(false);
		RequestParams params = new RequestParams();
		params.put("vote", vote);
		client.post(VOTING_URL, params, new AsyncHttpResponseHandler() {
			@Override
		    public void onSuccess(String response) {
				Toast.makeText(VotingActivity.this, "Stimme abgegeben", Toast.LENGTH_SHORT).show();
				setButtonsEnabled(true);
		    }
		});
	}
	
	private void setButtonsEnabled(boolean enabled) {
		a.setEnabled(enabled);
		b.setEnabled(enabled);
		c.setEnabled(enabled);
		d.setEnabled(enabled);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		continueToLoad = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		continueToLoad = true;
		handler.post(runnable);
	}

}
