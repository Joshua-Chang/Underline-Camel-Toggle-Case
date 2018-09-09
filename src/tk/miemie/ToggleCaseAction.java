package tk.miemie;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToggleCaseAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        //获取Editor和Project对象
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (editor == null || project == null)
            return;

        //获取SelectionModel和Document对象
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();

        selectionModel.selectWordAtCaret(false);
        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();

        //得到选中字符串的起始和结束位置
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();



        //对文档进行操作部分代码，需要放入Runnable接口中实现，由IDEA在内部将其通过一个新线程执行
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isLine(selectedText)) {
                    document.replaceString(startOffset, endOffset, lineToHump(selectedText));
                }else {
                    String temp=null;
                    if (selectedText.charAt(0) == 'm' && selectedText.charAt(1) <= 'Z' && selectedText.charAt(1) >= 'A') {//情况一：把mActivityInstance这种变为ActivityInstance
                        temp = selectedText.substring(1);
                    }
                    String result = temp != null ? temp : selectedText;
                    result=humpToLine(result);
                    if (result.charAt(0)=='_') {
                        result=result.substring(1);
                    }
                    document.replaceString(startOffset, endOffset, result);
                }
            }
        };

        //加入任务，由IDEA调度执行这个任务
        WriteCommandAction.runWriteCommandAction(project, runnable);

    }

    private String humpToLine2(String str) {
        return str.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    private Pattern humpPattern = Pattern.compile("[A-Z]");

    private String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private boolean isLine(String text){
        int length = text.length();
        boolean isLine=false;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i)=='_') {
                isLine=true;
            }
        }
        return isLine;
    }


    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        SelectionModel selectionModel = editor.getSelectionModel();

        selectionModel.selectWordAtCaret(false);
        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();

        //如果没有字符串被选中，那么无需显示该Action
        e.getPresentation().setVisible(selectedText != null && selectionModel.hasSelection());
    }
}
