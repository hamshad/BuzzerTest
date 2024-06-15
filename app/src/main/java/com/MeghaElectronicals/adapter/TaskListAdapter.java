package com.MeghaElectronicals.adapter;

import static com.MeghaElectronicals.common.MyFunctions.convertDate;
import static com.MeghaElectronicals.common.MyFunctions.nullCheck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.MeghaElectronicals.R;
import com.MeghaElectronicals.common.MySharedPreference;
import com.MeghaElectronicals.databinding.TaskListItemBinding;
import com.MeghaElectronicals.modal.TasksListModal;
import com.MeghaElectronicals.views.MainActivity;
import com.MeghaElectronicals.views.UpdateTaskDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    protected TaskListItemBinding ui;
    private List<TasksListModal> oldList;
    private List<TasksListModal> newList;
    private final List<TasksListModal> tasksList = new ArrayList<>();
    private final Context context;
    private final MainActivity activity;

    public TaskListAdapter(MainActivity mainActivity) {
        this.context = mainActivity.getApplicationContext();
        this.activity = mainActivity;
    }

    public void addTasksList(List<TasksListModal> newTasksList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffUtil(tasksList, newTasksList));
        tasksList.clear();
        tasksList.addAll(newTasksList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskListViewHolder(TaskListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder h, int position) {
        TasksListModal modal = tasksList.get(position);

        ui.employeeName.setText(nullCheck(modal.EmployeName()));
        ui.departmentName.setText(nullCheck(modal.Department()));
        ui.progress.setText(nullCheck(modal.Status()));
        ui.assignedBy.setText(setColoredText("Assigned by: ", nullCheck(modal.CreatedName()), true));
        ui.taskName.setText(nullCheck(modal.TaskName()));
        ui.taskDesc.setText(nullCheck(modal.Description()));
        ui.startDate.setText(setColoredText("Start\n", convertDate(nullCheck(modal.StartDate())), false));
        ui.endDate.setText(setColoredText("End\n", convertDate(nullCheck(modal.EndDate())), false));

        if (!modal.Status().equalsIgnoreCase("In-Progress")) {
            ui.completionDesc.setVisibility(View.VISIBLE);
            ui.completionDate.setVisibility(View.VISIBLE);
            ui.completionDesc.setText(setColoredText("Completion: ", modal.CompletionDescription(), true));
            ui.completionDate.setText(setColoredText("Completion\n", convertDate(modal.CompletionDate()), false));
        }

        setColor(modal.ColorsName());
        setImage(ui.statusImg, modal.ColorsName());

        String EmpId = new MySharedPreference(context).fetchLogin().EmpId();

        Log.d("TAG", "onBindViewHolder: "+modal.EmpId()+ " --- "+EmpId);
        Log.d("TAG", "onBindViewHolder: "+modal.CreatedBy());

        if ((EmpId.equals(modal.CreatedBy()) || EmpId.equals(modal.EmpId())) && modal.Status().equalsIgnoreCase("In-Progress")) {
            ui.updateTaskBtn.setVisibility(View.VISIBLE);
            ui.materialCardView.setOnClickListener(v -> {
//                activity.openBottomSheet();
                if (!UpdateTaskDialogFragment.isBottomSheetUp) {
                    UpdateTaskDialogFragment updateTaskDialog = UpdateTaskDialogFragment.newInstance(modal.TaskName(), modal.TaskId());
                    updateTaskDialog.show(activity.getSupportFragmentManager(), "UpdateTaskDialogFragment");
                }
            });
        } else {
            ui.materialCardView.setClickable(false);
            ui.materialCardView.setFocusable(false);
        }

//        try {
//            float progress = getProgress(modal);
//            h.ui.progressBar.setProgressCompat((int) progress, true);
//            h.ui.progressText.setText(String.valueOf(progress).concat("%"));
//        } catch (Exception e) {
//            e.fillInStackTrace();
//        }

    }
//
//    private float getProgress(TasksListModal modal) throws ParseException {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
//        Date startDate = sdf.parse(modal.StartDate());
//        Date endDate = sdf.parse(modal.EndDate());
//        long currentInMilli = Calendar.getInstance().getTimeInMillis();
//        long startInMilli = startDate.getTime();
//        long endInMilli = endDate.getTime();
//        long diffDate = endInMilli - startInMilli;
//        long diffCurrent = currentInMilli - startInMilli;
//        Log.d("PROGRESS", String.format("currentInMilli: %s, startInMilli: %s, endInMilli: %s, diffDate: %s, diffCurrent: %s, progress %s",
//                currentInMilli, startInMilli, endInMilli, diffDate, diffCurrent, (diffCurrent / diffDate * 100) / 10));
//        return (float) (diffCurrent / diffDate * 100) / 10;
//    }

    private void setColor(String s) {
        @SuppressLint("DiscouragedApi") int colorId = context.getResources().getIdentifier(s, "color", context.getPackageName());
        int color = ContextCompat.getColor(context, colorId);
        ui.materialCardView.setStrokeColor(color);
        ui.progressCard.setCardBackgroundColor(color);
//        ui.view.setBackgroundTintList(ColorStateList.valueOf(color));
//        ui.employeeName.setTextColor(Color.parseColor(s));
//        ui.departmentName.setTextColor(Color.parseColor(s));
    }

    private void setImage(ImageView statusImg, String s) {
        switch (s) {
            case "RED":
                statusImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hot));
                break;
            case "Blue":
                statusImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.cold));
                break;
            case "Yellow":
                statusImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.warm));
                break;
            case "Green":
                statusImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.chilled));
                break;
            default:
                statusImg.setVisibility(View.GONE);
        }
    }

    public SpannableString setColoredText(String prefix, String content, boolean setItalic) {
        if (nullCheck(content).equals("-")) return new SpannableString("-");
        String fullText = prefix + content;
        SpannableString spannableString = new SpannableString(fullText);
        int color = ContextCompat.getColor(context, R.color.tertiary);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (setItalic) spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, prefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    public class TaskListViewHolder extends RecyclerView.ViewHolder {

        public TaskListViewHolder(@NonNull TaskListItemBinding ui) {
            super(ui.getRoot());
            TaskListAdapter.this.ui = ui;
        }
    }

    private class MyDiffUtil extends DiffUtil.Callback {

        public MyDiffUtil(List<TasksListModal> oldList, List<TasksListModal> newList) {
            TaskListAdapter.this.oldList = oldList;
            TaskListAdapter.this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Compare unique TaskIds
            return oldList.get(oldItemPosition).toString().equals(newList.get(newItemPosition).toString());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            // Compare data
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}
