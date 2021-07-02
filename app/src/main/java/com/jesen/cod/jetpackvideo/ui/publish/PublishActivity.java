package com.jesen.cod.jetpackvideo.ui.publish;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityPublishBinding;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.libnavannotation.ActivityDestination;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPublishBinding mBinding;

    private int fileWidth, fileHeight;
    private String filePath;
    private boolean fileIsVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_publish);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_publish);

        mBinding.closeBtn.setOnClickListener(this);
        mBinding.publishBtn.setOnClickListener(this::onClick);
        mBinding.deleteFileBtn.setOnClickListener(this::onClick);
        mBinding.addTagBtn.setOnClickListener(this::onClick);
        mBinding.addFileBtn.setOnClickListener(this::onClick);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.delete_file_btn:
                clearAllFile();
                break;
            case R.id.publish_btn:
                break;
            case R.id.add_tag_btn:
                showTagBottomDialog();
                break;
            case R.id.add_file_btn:
                CaptureActivity.startActivityForResult(this);
                break;
            default:
        }
    }

    private void clearAllFile() {
        mBinding.addFileBtn.setVisibility(View.VISIBLE);
        mBinding.fileContainerLayout.setVisibility(View.GONE);
        mBinding.coverImage.setImageUrl(null);
        filePath = null;
        fileHeight = fileWidth = 0;
        fileIsVideo = false;
    }

    private void showTagBottomDialog() {
        TagBottomSheetDialog sheetDialog = new TagBottomSheetDialog();
        sheetDialog.setTagItemSelectListener(new TagBottomSheetDialog.OnTagItemSelectListener() {
            @Override
            public void onSelectTag(TagList tagList) {
                mBinding.addTagBtn.setText(tagList.title);
            }
        });

        sheetDialog.show(getSupportFragmentManager(), "tag_dialog");
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.publish_exit_message)
                .setNegativeButton(R.string.publish_exit_action_cancel, null)
                .setPositiveButton(R.string.publish_exit_action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        PublishActivity.this.finish();
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE_TO_CAPTURE && resultCode == RESULT_OK || data != null) {
            fileWidth = data.getIntExtra(CaptureActivity.PREVIEW_RESULT_FILE_WIDTH, 0);
            fileHeight = data.getIntExtra(CaptureActivity.PREVIEW_RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.PREVIEW_RESULT_FILE_PATH);
            fileIsVideo = data.getBooleanExtra(CaptureActivity.PREVIEW_RESULT_FILE_TYPE, false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        mBinding.addFileBtn.setVisibility(View.GONE);
        mBinding.fileContainerLayout.setVisibility(View.VISIBLE);
        mBinding.coverImage.setImageUrl(filePath);
        mBinding.videoIcon.setVisibility(fileIsVideo ? View.VISIBLE : View.GONE);
        mBinding.coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviewActivity.startActivityForResult(PublishActivity.this, filePath,
                        fileIsVideo, null);
            }
        });
    }
}