package asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import helperClasses.DatabaseManager;
import helperClasses.ConfirmationEmailManager;
import helperClasses.SharedPreferencesManager;

import static net.scaniq.scaniqairprint.MainActivity.MYSQLRRuid;

/**
 * Created by savanpatel on 2017-02-02.
 */

public class NewUserEmail extends AsyncTask<String, String, String> {

    // Declare UI elemnts here
    Context context;

    public NewUserEmail(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // show ProgressDialog and initilize Ui elemnts
    }

    protected String doInBackground(String... args) {

        try {
            sendNewUserEmail(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        return null;
    }

    protected void onPostExecute(String file_url) {
        // Update UI when doInBackground exection completes
    }

    private void sendNewUserEmail(String newuser) throws SQLException, ClassNotFoundException {
        String scaniq_rrid = SharedPreferencesManager.getInstance().getScaniqRrid();
        MYSQLRRuid = scaniq_rrid;

        scaniq_rrid = registerNextUnit(newuser);

//        SharedPreferencesManager.getInstance().setScaniqRrid(scaniq_rrid); //Don't need this line!!!
        MYSQLRRuid = scaniq_rrid;
        SharedPreferencesManager.getInstance().setScaniqEmail(false);
        ConfirmationEmailManager.getInstance().sendMail(newuser,MYSQLRRuid);
    }

    private String registerNextUnit(String emailAcct) throws SQLException, ClassNotFoundException {
        TelephonyManager manager = (TelephonyManager)context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        Connection con = DatabaseManager.getInstance().getConnection(context);// 1st parameter
        String carrierName = manager.getNetworkOperatorName();// 2nd parameter
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);// 3nd parameter

        ResultSet rs = DatabaseManager.getInstance().executeRegisterPreparedStatement(con,emailAcct,androidId,carrierName,context);

        String new_scaniq_rrid = "";
        String new_md5 = "";

        if(rs.next()){
            new_scaniq_rrid = rs.getString(1);
            new_md5 = rs.getString(2);
            SharedPreferencesManager.getInstance().setScaniqRrid(new_scaniq_rrid);
            SharedPreferencesManager.getInstance().setScaniqMd5(new_md5);
        }
        DatabaseManager.getInstance().closeConnection(con,context);
        return new_scaniq_rrid;
    }
}