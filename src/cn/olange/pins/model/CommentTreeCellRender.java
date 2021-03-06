package cn.olange.pins.model;

import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class CommentTreeCellRender extends JPanel implements TreeCellRenderer {
    private static final int MARGIN = 2;
    private JBLabel user;
    private JPanel userActionPanel;
    private final ColoredTreeCellRenderer treeCellRenderer;
    private ColoredTreeCellRenderer  moreCellRenderer;
    ColoredTreeCellRenderer replyActionRender;
    public CommentTreeCellRender() {
        setLayout(new BorderLayout());
        userActionPanel = new JPanel();
        userActionPanel.setLayout(new BorderLayout());
        user = new JBLabel();
        user.setForeground(JBColor.blue);
        treeCellRenderer = new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree jTree, Object value, boolean b, boolean b1, boolean b2, int i, boolean b3) {
                this.append((String) value, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                setBorder(null);
            }
        };
        userActionPanel.add(user, BorderLayout.WEST);
        replyActionRender = new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree jTree, Object value, boolean b, boolean b1, boolean b2, int i, boolean b3) {
                if (value != null && StringUtils.isNotEmpty(value.toString())) {
                    this.append(" 回复 ", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                    this.append(value.toString(), new SimpleTextAttributes(0, JBColor.BLUE));
                }
                this.append(":", new SimpleTextAttributes(0, JBColor.BLUE));
                setBorder(null);
            }
        };
        userActionPanel.add(replyActionRender, BorderLayout.CENTER);
        add(treeCellRenderer, BorderLayout.CENTER);
        add(userActionPanel, BorderLayout.WEST);
        moreCellRenderer = new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3) {
                this.setFont(UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL)));
                this.append("...", SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
                this.setMyBorder(null);
            }
        };
        setBorder(JBUI.Borders.empty(MARGIN, MARGIN, MARGIN, 0));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Config config = JuejinPersistentConfig.getInstance().getState();
        Color color = selected ? tree.getForeground() : tree.getBackground();
        setBackground(color);
        treeCellRenderer.setBackground(color);
        replyActionRender.setBackground(color);
        user.setBackground(color);
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) value;
        if (mutableTreeNode.getUserObject() instanceof CommentNode) {
            CommentNode commentTreeNode = (CommentNode) mutableTreeNode.getUserObject() ;
            JsonObject user_info = commentTreeNode.getItem().get("user_info").getAsJsonObject();
            if (config.isLogined() && Comparing.strEqual(config.getUserId(), user_info.get("user_id").getAsString())) {
                user.setForeground(JBColor.GREEN);
            }else {
                user.setForeground(JBColor.blue);
            }
            String userName = user_info.get("user_name").getAsString();
            if (commentTreeNode.getItem().get("is_author").getAsBoolean()) {
                this.user.setText(userName + " (作者)");
            } else {
                this.user.setText(userName);
            }
            if (commentTreeNode.getLevel() == 1) {

                replyActionRender.getTreeCellRendererComponent(tree, null, selected, expanded, leaf, row, hasFocus);
                String commentContent = commentTreeNode.getItem().getAsJsonObject("comment_info").get("comment_content").getAsString();
                treeCellRenderer.getTreeCellRendererComponent(tree, commentContent, selected, expanded, leaf, row, hasFocus);
            } else if (commentTreeNode.getLevel() == 2) {

                JsonElement reply_user = commentTreeNode.getItem().get("reply_user");
                if (reply_user == null || reply_user.isJsonNull()) {
                    replyActionRender.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
                }else {
                    replyActionRender.getTreeCellRendererComponent(tree, reply_user.getAsJsonObject().get("user_name").getAsString(), selected, expanded, leaf, row, hasFocus);
                }
                String commentContent = commentTreeNode.getItem().getAsJsonObject("reply_info").get("reply_content").getAsString();
                treeCellRenderer.getTreeCellRendererComponent(tree, commentContent, selected, expanded, leaf, row, hasFocus);
            }
        } else if (mutableTreeNode.getUserObject()  instanceof NeedMore) {
            return moreCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }else {
            this.user.setText("评论列表");
            treeCellRenderer.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
        }
        return this;
    }
}
