package com.tomclaw.mandarin.main.icq;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.tomclaw.mandarin.R;
import com.tomclaw.mandarin.core.*;
import com.tomclaw.mandarin.core.exceptions.AccountAlreadyExistsException;
import com.tomclaw.mandarin.im.StatusUtil;
import com.tomclaw.mandarin.im.icq.IcqAccountRoot;
import com.tomclaw.mandarin.im.icq.RegistrationHelper;
import com.tomclaw.mandarin.main.ChiefActivity;
import com.tomclaw.mandarin.main.MainActivity;

/**
 * Created by Solkin on 28.09.2014.
 */
public class PhoneLoginActivity extends Activity {

    private static int REQUEST_CODE_COUNTRY = 1;

    TextView countryCodeField;
    EditText phoneNumberField;

    RegistrationHelper.RegistrationCallback callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.icq_phone_login);

        // Initialize action bar.
        ActionBar bar = getActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setIcon(R.drawable.ic_ab_logo);

        /*countryCodeField = (TextView) findViewById(R.id.country_code_field);
        countryCodeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(PhoneLoginActivity.this, CountryCodeActivity.class),
                        REQUEST_CODE_COUNTRY);
            }
        });*/

        phoneNumberField = (EditText) findViewById(R.id.phone_number_field);
        phoneNumberField.setText("+7"); // TODO: detect country code automatically
        phoneNumberField.setSelection(phoneNumberField.getText().length());
        phoneNumberField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        callback = new RegistrationHelper.RegistrationCallback() {
            @Override
            public void onPhoneNormalized(String msisdn) {
                RegistrationHelper.validatePhone(msisdn, callback);
            }

            @Override
            public void onPhoneValidated(final String msisdn, final String transId) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onSmsSent(msisdn, transId);
                    }
                });
            }

            @Override
            public void onPhoneLoginSuccess(String login, String tokenA, String sessionKey, long expiresIn, long hostTime) {
            }

            @Override
            public void onProtocolError() {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onRequestError();
                    }
                });
            }

            @Override
            public void onNetworkError() {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onRequestError();
                    }
                });
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflateMenu(menu, R.menu.phone_enter_menu, R.id.phone_enter_menu);
        return true;
    }

    private void inflateMenu(final Menu menu, int menuRes, int menuItem) {
        getMenuInflater().inflate(menuRes, menu);
        final MenuItem item = menu.findItem(menuItem);
        TextView actionView = ((TextView) item.getActionView());
        actionView.setText(actionView.getText().toString().toUpperCase());
        actionView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                menu.performIdentifierAction(item.getItemId(), 0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.phone_enter_menu: {
                String countryCode = countryCodeField.getText().toString();
                String phoneNumber = phoneNumberField.getText().toString();
                requestSms(countryCode, phoneNumber);
                break;
            }
        }
        return true;
    }

    private void requestSms(final String countryCode, final String phoneNumber) {
        RegistrationHelper.normalizePhone(countryCode, phoneNumber, callback);
    }

    private void onSmsSent(String msisdn, String transId) {
        startActivity(new Intent(this, SmsCodeActivity.class)
                .putExtra(SmsCodeActivity.EXTRA_MSISDN, msisdn)
                .putExtra(SmsCodeActivity.EXTRA_TRANS_ID, transId));
    }

    private void onRequestError() {
        Toast.makeText(this, "Error. Try again.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_COUNTRY && resultCode == RESULT_OK) {
            int code = data.getIntExtra(CountryCodeActivity.EXTRA_COUNTRY_CODE, 0);
            if(code != 0) {
                countryCodeField.setText("+" + code);
            }
        }
    }
}
