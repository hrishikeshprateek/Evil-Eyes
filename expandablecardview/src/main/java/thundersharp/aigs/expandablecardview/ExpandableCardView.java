package thundersharp.aigs.expandablecardview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * @author hrishikeshprateek
 * @see android.view.ViewGroup
 * @see android.view.ViewGroup
 * @see androidx.cardview.widget.CardView
 */
public class ExpandableCardView extends RelativeLayout {

    private View view;
    private RelativeLayout top_container;
    private RelativeLayout bottom_container;
    private TextView date,time,countOfTheDay,missedCalls,incomingCalls,outgoingCalls;
    private ProgressBar progressBar;
    private ImageView imageView;
    private OnCardExpandedListener onCardExpandedListener;
    private boolean isExpanded;
    private Integer incomingCount, outgoingCount,missedCount, callCount;
    private View innerView;


    public ExpandableCardView(@NonNull Context context) {
        super(context);
        inflateViews(context,null);
    }

    public ExpandableCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflateViews(context, attrs);
    }

    public ExpandableCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateViews(context, attrs);
    }


    public void setDate(String dateData){
        date.setText(dateData);
    }

    public void setDuration(String duration){
        time.setText(duration);
    }

    public void setCallCount(int count){
        this.callCount = count;
        countOfTheDay.setText(count+"");
    }

    public void setIncomingCallCount(int count){
        this.incomingCount = count;
        incomingCalls.setText("Incoming calls count:"+count);
    }

    public void setOutgoingCallsCount(int count){
        this.outgoingCount = count;
        outgoingCalls.setText("Outgoing calls count:"+count);
    }

    public void setOnCardExpandedListener(OnCardExpandedListener onCardExpandedListener){
        this.onCardExpandedListener = onCardExpandedListener;
    }

    public void setMissedCallCount(int count){
        this.missedCount = count;
        missedCalls.setText("Missed calls count:"+count);
    }

    public void initializeProgress() throws Exception{
        if (callCount ==null ||incomingCount == null || outgoingCount ==null || missedCount == null) throw new Exception("Cannot render progress all params not set !!");
        progressBar.setMax(callCount);
        progressBar.setProgress(incomingCount,true);
        progressBar.setSecondaryProgress((outgoingCount+incomingCount));
    }

    public void setInnerExpansionView(View layout){
        addView(layout);
    }

    public void setPrimaryView(int primaryView){

    }

    private void inflateViews(@NonNull Context context, @Nullable AttributeSet attrs){
        view = inflate(context, R.layout.expandable_card_view,this);

        top_container = view.findViewById(R.id.top_data_holder);
        bottom_container = view.findViewById(R.id.nestedView);
        imageView = view.findViewById(R.id.expand_collapse_icon);
        date = view.findViewById(R.id.date);
        time = view.findViewById(R.id.timeTotal);
        countOfTheDay = view.findViewById(R.id.callCount);
        incomingCalls = view.findViewById(R.id.incomingCalls);
        outgoingCalls = view.findViewById(R.id.outgoingCalls);
        missedCalls = view.findViewById(R.id.missedCalls);
        progressBar = view.findViewById(R.id.progressCall);

        top_container.setOnClickListener(n -> {
            if (isExpanded){
                isExpanded = false;
                bottom_container.setVisibility(GONE);
                if (onCardExpandedListener != null) onCardExpandedListener.onCardCollapsed();
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_expand_more_24));
            }else {
                isExpanded = true;
                if (onCardExpandedListener != null) onCardExpandedListener.onCardExpanded();
                bottom_container.setVisibility(VISIBLE);
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_expand_less_24));

            }
        });



    }

}