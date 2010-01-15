/**
 *  This program is free software; you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 3 of the License, or (at your option) any later 
 *  version.
 *  You should have received a copy of the GNU General Public License along with 
 *  this program; if not, see <http://www.gnu.org/licenses/>. 
 *  Use this application at your own risk.
 *
 *  Copyright (c) 2010 Alexander Chukov <sash@pdaXrom.org>
 */

package com.pdaXrom.android.gprswall;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class GPRSWall extends Activity {
	private ImageView imageView;
    private TextView outputView;
	private Button switchButton;
	private Handler handler = new Handler();

	private String commands_check[] = { 
			"if iptables -vL | grep -q 'REJECT.*pdp0' ; then",
			"	echo \"Access to GPRS disabled\"",
			"else",
			"	echo \"Access to GPRS enabled\"",
			"fi"
	};

	private String commands_switch[] = { 
			"if iptables -vL | grep -q 'REJECT.*pdp0' ; then",
			"	iptables -D OUTPUT -o pdp0 -j REJECT",
			"	echo \"Access to GPRS enabled\"",
			"else",
			"	iptables -A OUTPUT -o pdp0 -j REJECT",
			"	echo \"Access to GPRS disabled\"",
			"fi"
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        imageView = (ImageView)findViewById(R.id.ImageView);
        outputView = (TextView)findViewById(R.id.OutputView);
        switchButton = (Button)findViewById(R.id.SwitchButton);
        switchButton.setOnClickListener(onSwitchButtonClick);

        String res = doExec(commands_check);
		if (res.contains("disabled"))
			imageView.setImageResource(R.drawable.disable);
		else if (res.contains("enabled"))
			imageView.setImageResource(R.drawable.enable);
		output(res);
    }

	private OnClickListener onSwitchButtonClick = new OnClickListener() {
		public void onClick(View v) {
			String res = doExec(commands_switch);
			if (res.contains("disabled"))
				imageView.setImageResource(R.drawable.disable);
			else if (res.contains("enabled"))
				imageView.setImageResource(R.drawable.enable);
			output(res);
		}
	};

	private String doExec(String commands[]) {
		String res = "";

		try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			BufferedReader osRes = new BufferedReader(new InputStreamReader(process.getInputStream()));
			for (String single : commands) {
				os.writeBytes(single + "\n");
				os.flush();
				Thread.sleep(200);

				try {
					while (osRes.ready()) {
						res += osRes.readLine() + "\n";
					}
				} catch (IOException e) {
					// It seems IOException is thrown when it reaches EOF.
				}					
			}
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			res += "ERROR " + e.getMessage() + "\n";
		}
		
		return res;
	};
	
	private void output(final String str) {
    	Runnable proc = new Runnable() {
			public void run() {
				outputView.setText(str);
			}
    	};
    	handler.post(proc);
    };
}
