package com.tomclaw.mandarin.main;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.tomclaw.mandarin.R;
import com.tomclaw.mandarin.core.QueryHelper;
import com.tomclaw.mandarin.core.Settings;
import com.tomclaw.mandarin.core.exceptions.AccountAlreadyExistsException;
import com.tomclaw.mandarin.im.AccountRoot;

/**
 * Created with IntelliJ IDEA.
 * User: lapshin
 * Date: 4/17/13
 * Time: 4:07 PM
 */
public class AccountAddActivity extends ChiefActivity {

    public static final String EXTRA_CLASS_NAME = "class_name";
    private AccountRoot accountRoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain class name extra to setup AccountRoot type.
        String className = getIntent().getStringExtra(EXTRA_CLASS_NAME);
        Log.d(Settings.LOG_TAG, "AccountAddActivity start for " + className);
        try {
            Class<? extends AccountRoot> accountRootClass = Class.forName(className).asSubclass(AccountRoot.class);
            accountRoot = accountRootClass.newInstance();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_add_menu, menu);
        return true;
    }

    @Override
    public void onCoreServiceReady() {
        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setTitle(R.string.accounts);
        // Initialize accounts list
        setContentView(accountRoot.getAccountLayout());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(Settings.LOG_TAG, "onOptionsItemSelected: " + item.getTitle());
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.ok_account_menu: {
                String userId = ((EditText) findViewById(R.id.user_id_field)).getText().toString();
                String userPassword = ((EditText) findViewById(R.id.user_password_field)).getText().toString();
                // Check for credentials are filed correctly
                if (TextUtils.isEmpty(userId)) {
                    Toast.makeText(AccountAddActivity.this, R.string.user_id_empty, Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(userPassword)) {
                    Toast.makeText(AccountAddActivity.this, R.string.user_password_empty, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        accountRoot.setUserId(userId);
                        accountRoot.setUserNick(userId);
                        accountRoot.setUserPassword(userPassword);
                        int accountDbId = QueryHelper.insertAccount(this, accountRoot);
                        getServiceInteraction().holdAccount(accountDbId);
                        // Creating signal intent.
                        setResult(RESULT_OK);
                        finish();
                    } catch (AccountAlreadyExistsException ex) {
                        Toast.makeText(AccountAddActivity.this, R.string.account_already_exists, Toast.LENGTH_LONG).show();
                    } catch (Throwable ex) {
                        Toast.makeText(AccountAddActivity.this, R.string.account_add_fail, Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onCoreServiceDown() {
    }

    @Override
    public void onCoreServiceIntent(Intent intent) {
    }
}
