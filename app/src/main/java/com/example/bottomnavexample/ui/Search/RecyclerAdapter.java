package com.example.bottomnavexample.ui.Search;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottomnavexample.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

        private List<String> data;
        private LayoutInflater layoutInflater;
        public static String name;
        public static Double lat,lon;
        private String uid=SearchFragment.uid;
        private Location mloc=SearchFragment.mloc;
        private DocumentReference myplace;
        private static final String NAME = "Name";
        private static final String LATITUDE = "Latitude";
        private static final String LONGITUDE= "Longitude";
        FirebaseFirestore fb;

    public RecyclerAdapter(Context context,List<String> data) {
        this.data = data;
        this.layoutInflater=LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_layout,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String places=data.get(position);
        viewHolder.textView.setText(places);
    }

    @Override
    public int getItemCount() {

        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fb=FirebaseFirestore.getInstance();
            textView=itemView.findViewById(R.id.row);
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    name=textView.getText().toString();
                    Intent intent = new Intent (v.getContext(), Edit_Delete.class);
                    v.getContext().startActivity(intent);
                    return false;
                }
            });
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   name=textView.getText().toString();
                   final Context context=v.getContext();
                   myplace=fb.collection("users").document(uid).collection("SavedPlaces").document(name);
                   myplace.get()
                     .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                       @Override
                       public void onSuccess(DocumentSnapshot doc) {
                           if ((doc.exists())){
                               String name1=doc.getString(NAME);
                               Double latitude=doc.getDouble(LATITUDE);
                               Double longitude=doc.getDouble(LONGITUDE);
                               if (name.equals(name1)){
                                   lat=latitude;
                                   lon=longitude;
                                   String uri = "http://maps.google.com/maps?saddr=" +mloc.getLatitude()+ "," +mloc.getLongitude() + "&daddr=" + lat + "," + lon;
                                   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                   context.startActivity(intent);
                                   }
                               }else{

                               }
                           }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                       }
                   });
                }
            });
        }
    }
}
