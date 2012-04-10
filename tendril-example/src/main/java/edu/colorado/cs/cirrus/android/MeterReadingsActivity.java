package edu.colorado.cs.cirrus.android;

import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.widget.TextView;
import edu.colorado.cs.cirrus.android.task.PricingProgramTask;
import edu.colorado.cs.cirrus.android.task.MeterReadingsTask;
import edu.colorado.cs.cirrus.domain.model.MeterReading;
import edu.colorado.cs.cirrus.domain.model.MeterReadings;
import edu.colorado.cs.cirrus.domain.model.PricingProgram;

public class MeterReadingsActivity extends AbstractAsyncTendrilActivity {
    protected static final String TAG = MeterReadingsActivity.class.getSimpleName();

    // ***************************************
    // Activity methods
    // ***************************************
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tendril_meter_reading_layout);

    }

    @Override
    public void onStart() {

        super.onStart();

        MeterReadings meterReadings = null;
        try {
            // meterReading = (new MeterReadingTask()).execute(tendril).get();
            meterReadings = tendril.asyncGetMeterReadings();
            
            TextView meterReadingsData = (TextView) findViewById(R.id.textView1);
            TextView customerAgreement = (TextView) findViewById(R.id.meter_reading_agreement);
            TextView meterAsset = (TextView) findViewById(R.id.meter_asset);
            
            customerAgreement.setText(meterReadings.getMeterReading().getCustomerAgreement().getmRID());
            meterAsset.setText(meterReadings.getMeterReading().getMeterAsset().getmRID());
            meterReadingsData.setText(meterReadings.getMeterReading().getReadings().toString());

        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // String profile = new UserProfileTask().execute("").get();
        System.err.println(meterReadings);

        // String profile = task.execute();

    }

}