package thundersharp.aigs.expandablecardview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
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
    private TextView tittle,desc;

    private ImageView imageView;

    private boolean isExpanded;
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


    public void setTittle(String tittleText){
        tittle.setText(tittleText);
    }

    public void setDescription(String description){
        desc.setText(description);
    }

    public void setInnerExpansionView(View layout){
        addView(layout);
    }

    public void setPrimaryView(int primaryView){
        this.
    }

    private void inflateViews(@NonNull Context context, @Nullable AttributeSet attrs){
        view = inflate(context, R.layout.expandable_card_view,this);

        top_container = view.findViewById(R.id.top_data_holder);
        bottom_container = view.findViewById(R.id.nestedView);
        imageView = view.findViewById(R.id.expand_collapse_icon);
        tittle = view.findViewById(R.id.tittle);
        desc = view.findViewById(R.id.desc);

        top_container.setOnClickListener(n -> {
            if (isExpanded){
                isExpanded = false;
                bottom_container.setVisibility(GONE);
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_expand_more_24));
            }else {
                isExpanded = true;
                bottom_container.setVisibility(VISIBLE);
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_expand_less_24));

            }
        });



    }

}