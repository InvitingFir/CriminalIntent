package com.bignerdranch.criminalintent.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bignerdranch.criminalintent.data.Crime;
import com.bignerdranch.criminalintent.data.CrimeLab;
import com.bignerdranch.criminalintent.R;

import java.util.Collections;
import java.util.List;

/**
 * Класс для отображения списка преступлений
 */
public class CrimeListFragment extends Fragment{
    private static final String SAVED_SUBTITLE_VISIBLE = "saved subtitle visible";

    private RecyclerView mRecyclerView;
    private CrimeAdapter mCrimeAdapter;
    private boolean mSubtitleIsVisible;
    private Button mNewCrimeButton;

    private Callbacks mCallbacks;

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * метод для установления макета во фрагменте
     * @param inflater - объект преобразования макета во View
     * @param container - родительский объект ViewGroup
     * @param savedInstanceState - дополнительные параметры
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState!=null)
            mSubtitleIsVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        View view = inflater.inflate(R.layout.fragment_layout_list, container, false);
        mRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNewCrimeButton = view.findViewById(R.id.new_crime_button);
        mNewCrimeButton.setOnClickListener(l-> createNewCrime());
        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);
        updateUI();
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleIsVisible);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.new_crime:
                createNewCrime();
                return true;
            case R.id.show_subtitle:
                getActivity().invalidateOptionsMenu();
                mSubtitleIsVisible=!mSubtitleIsVisible;
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * обновление параметров отображения
     */
    public void updateUI(){
        CrimeLab cl = CrimeLab.getInstance(getActivity());
        if(mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(cl.getCrimes());
            mRecyclerView.setAdapter(mCrimeAdapter);
        }
        else {
            mCrimeAdapter.setCrimes(cl.getCrimes());
            mCrimeAdapter.notifyDataSetChanged();
        }
        if(cl.getCrimes().size() == 0) mNewCrimeButton.setVisibility(View.VISIBLE);
        else mNewCrimeButton.setVisibility(View.INVISIBLE);
        updateSubtitle();
    }

    /**
     * Этот класс необходим для создания строчек списка.
    * Создается определенное количество объектов строк
    * для списка, после чего значения в этих строчках
    * просто меняются в соответствии с необходимыми
    * значениями.
     * */
    private class CrimeHolder extends RecyclerView.ViewHolder {
        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private ImageView mCrimeSolvedImage;
        private Crime mCrime;

        /**
         * конструктор устанавливает внешний вид элемента списка RecycleView
         * @param inflater - метод для преобразования макета во View
         * @param layout - идентификатор макета layout
         * @param parent - родительский объект ViewGroup
         */
        private CrimeHolder(LayoutInflater inflater, int layout, ViewGroup parent) {
            super(inflater.inflate(layout, parent, false));
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mCrimeSolvedImage = itemView.findViewById(R.id.crime_solved_image);
            itemView.setOnClickListener(l ->{
                mCallbacks.onCrimeSelected(mCrime);
            });
        }

        /**
         * Устанавливает значение в конкретной строке списка RecycleView
         * @param c - Объект Crime, чбе значение устанавливается в строку
         */
        public void bind(Crime c){
            String date;
            String description;
            String isSolved = c.isSolved()?
                    getResources().getString(R.string.crime_report_solved) : getResources().getString(R.string.crime_report_unsolved);
            mCrime = c;
            date = DateFormat.format("EEEE, dd MMM, yyyy", c.getDate()).toString();
            description = getResources().getString(R.string.crime_description, c.getTitle(), date, isSolved);
            itemView.setContentDescription(description);
            mTitleTextView.setText(c.getTitle());
            mDateTextView.setText(date);
            mCrimeSolvedImage.setVisibility(c.isSolved()? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Этот класс занимается созданием и добавлением в список
     * новых необходимых строк и перезаписыванием в эти строки
     * требуемой информации
     */
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        public static final int REQUIRES_POLICE = 0;
        public static final int NO_POLICE_REQUIRED = 1;
        private List<Crime> mCrimes;

        /**
         * Конструктор
         * @param crimes - список, в соответствии с которым будет
         *               меняться отображаемая информация
         */
        public CrimeAdapter(List<Crime> crimes){
            this.mCrimes = crimes;
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

        /**
         * Создание объекта отображения новой строки для RecycleView
         * @param parent - объект View, внутри которого будут размещаться
         *               объекты строк ViewHolder
         * @param viewType - позволяет различать между собой объекты ViewHolder
         *                 и, например, задавать им разные макеты
         * @return возвращает объект отображения ViewHolder
         */
        @NonNull
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(inflater, R.layout.list_item_crime, parent);
        }

        /**
         * Метод для обновления информации на конкретной строке recycleView
         * @param holder - объект отображения конкретной строки списка
         * @param position - позиция этого отображения в списке
         */
        public void onBindViewHolder(CrimeHolder holder, int position) {
            holder.bind(mCrimes.get(position));
        }

        /**
         * сеттер размера списка для отображения
         * @return возврщает длину списка
         */
        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        /**
         * установить тип отображаемого значения(для смены макета, например)
         * @param position - позиция отображаемого объекта в списке
         * @return возвращает тип отображаемого объекта
         */
        @Override
        public int getItemViewType(int position) {
            boolean b = mCrimes.get(position).isRequiresPolice();
            return b ? REQUIRES_POLICE : NO_POLICE_REQUIRED;
        }

        public boolean onItemMove(int fromPosition, int toPosition) {
            if(fromPosition > toPosition){
                for(int i = fromPosition; i < toPosition; i++){
                    Collections.swap(mCrimes, i, i+1);
                }
            }
            else{
                for(int i = fromPosition; i > toPosition; i--){
                    Collections.swap(mCrimes, i, i-1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        public void onItemDismiss(int position) {
            Crime c = mCrimes.get(position);
            CrimeLab.getInstance(getContext()).removeCrime(c);
            mCrimes.remove(position);
            updateUI();
        }
    }

    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            mCrimeAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mCrimeAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem mi = menu.findItem(R.id.show_subtitle);
        if(mSubtitleIsVisible) mi.setTitle(R.string.hide_subtitle);
        else mi.setTitle(R.string.show_subtitle);
    }

    private void updateSubtitle(){
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        CrimeLab lab = CrimeLab.getInstance(getActivity());
        int crimeSize = lab.getCrimes().size();
        String subtitle;
        if(mSubtitleIsVisible) subtitle =
                getResources().getQuantityString(R.plurals.subtitle_plurals, crimeSize, crimeSize
        );
        else subtitle = null;
        activity.getSupportActionBar().setSubtitle(subtitle);
    }
    
    private void createNewCrime(){
        Crime c = new Crime();
        CrimeLab.getInstance(getActivity()).addCrime(c);
        updateUI();
        mCallbacks.onCrimeSelected(c);
    }
}