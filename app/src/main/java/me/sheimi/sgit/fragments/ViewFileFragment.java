package me.sheimi.sgit.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.CodeGuesser;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.ViewFileActivity;
import me.sheimi.sgit.editor.SoraEditor;
import timber.log.Timber;

/**
 * Native code viewer/editor backed by sora-editor with TextMate syntax highlighting.
 * Created by phcoder on 09.12.15.
 */
public class ViewFileFragment extends BaseFragment {
    private CodeEditor mEditor;
    private ProgressBar mLoading;
    private File mFile;
    private short mActivityMode = ViewFileActivity.TAG_MODE_NORMAL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_file, container, false);

        mEditor = (CodeEditor) v.findViewById(R.id.fileContent);
        mLoading = (ProgressBar) v.findViewById(R.id.loading);

        String fileName = null;
        if (savedInstanceState != null) {
            fileName = savedInstanceState.getString(ViewFileActivity.TAG_FILE_NAME);
            mActivityMode = savedInstanceState.getShort(ViewFileActivity.TAG_MODE, ViewFileActivity.TAG_MODE_NORMAL);
        }
        if (fileName == null) {
            fileName = getArguments().getString(ViewFileActivity.TAG_FILE_NAME);
            mActivityMode = getArguments().getShort(ViewFileActivity.TAG_MODE, ViewFileActivity.TAG_MODE_NORMAL);
        }

        mFile = new File(fileName);
        configureEditor();
        loadFileContent();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mEditor != null) {
            mEditor.release();
            mEditor = null;
        }
    }

    private void configureEditor() {
        mEditor.setColorScheme(SoraEditor.createColorScheme(getContext()));
        mEditor.setEditable(mActivityMode != ViewFileActivity.TAG_MODE_SSH_KEY);
        mEditor.setUndoEnabled(true);
        mEditor.setLineNumberEnabled(true);
        mEditor.setWordwrap(true);
        mEditor.setTabWidth(4);
        mEditor.setTextSize(13);
        mEditor.setHighlightCurrentLine(true);
    }

    private void loadFileContent() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final String content;
                try {
                    content = FileUtils.readFileToString(mFile, "UTF-8");
                } catch (IOException e) {
                    showUserError(e, R.string.error_can_not_open_file);
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditor.setText(content);
                        if (mActivityMode != ViewFileActivity.TAG_MODE_SSH_KEY) {
                            setEditorLanguage(CodeGuesser.guessCodeType(mFile.getName()));
                        }
                        mLoading.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        thread.start();
    }

    public File getFile() {
        return mFile;
    }

    public void copyAll() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("mgit", mEditor.getText().toString());
        clipboard.setPrimaryClip(clip);
    }

    public void setLanguage(String lang) {
        setEditorLanguage(lang);
    }

    private void setEditorLanguage(String scopeName) {
        if (scopeName == null || scopeName.isEmpty()) {
            return;
        }
        try {
            mEditor.setEditorLanguage(TextMateLanguage.create(scopeName, true));
        } catch (Exception e) {
            Timber.w(e, "Could not load TextMate language for scope %s", scopeName);
            retryWhenGrammarsReady(scopeName);
        }
    }

    private void retryWhenGrammarsReady(final String scopeName) {
        SoraEditor.whenGrammarsReady(new Runnable() {
            @Override
            public void run() {
                if (mEditor == null) {
                    return;
                }
                try {
                    mEditor.setEditorLanguage(TextMateLanguage.create(scopeName, true));
                } catch (Exception e) {
                    Timber.w(e, "Retry failed for scope %s", scopeName);
                }
            }
        });
    }

    public boolean save() {
        if (mActivityMode == ViewFileActivity.TAG_MODE_SSH_KEY || !mEditor.isEditable()) {
            return false;
        }
        try {
            FileUtils.writeStringToFile(mFile, mEditor.getText().toString(), "UTF-8");
            return true;
        } catch (IOException e) {
            showUserError(e, R.string.error_can_not_save_file);
            return false;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public SheimiFragmentActivity.OnBackClickListener getOnBackClickListener() {
        return new SheimiFragmentActivity.OnBackClickListener() {
            @Override
            public boolean onClick() {
                return false;
            }
        };
    }


    private void showUserError(Throwable e, final int errorMessageId) {
        Timber.e(e);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SheimiFragmentActivity)getActivity()).
                    showMessageDialog(R.string.dialog_error_title, getString(errorMessageId));
            }
        });
    }
}
