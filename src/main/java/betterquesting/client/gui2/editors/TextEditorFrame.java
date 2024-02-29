package betterquesting.client.gui2.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang3.SystemUtils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.FLASHWINFO;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.handlers.NetQuestEdit;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class TextEditorFrame extends JFrame {

    private static final int initialRowCount = 30;
    private static final int defaultColumns = 60;
    private static final IntObjectMap<TextEditorFrame> opened = new IntObjectHashMap<>();// questId -> TextEditorFrame
    private static BufferedImage logoCache = null;

    public static void openTextEditor(int questID, IQuest quest) {
        if (opened.containsKey(questID)) {
            TextEditorFrame frame = opened.get(questID);
            frame.toFront();
            frame.requestFocus();
            Toolkit.getDefaultToolkit().beep();
            FlashWindowHelper.flash(frame);
        } else {
            TextEditorFrame frame = new TextEditorFrame(questID, quest);
            opened.put(questID, frame);
            frame.requestFocus();
        }
    }

    private final int questID;
    public final IQuest quest;
    private final String originalName;
    private final String originalDescription;

    private final JTextField name;
    private final JTextArea description;

    private TextEditorFrame(int questID, IQuest q) {
        super("Better Questing Text Editor | " + q.getProperty(NativeProps.NAME));
        this.questID = questID;
        quest = q;
        originalName = q.getProperty(NativeProps.NAME);
        originalDescription = q.getProperty(NativeProps.DESC);

        if (logoCache == null) {
            try (InputStream stream = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(new ResourceLocation(ModReference.MODID, "textures/items/quest_book.png"))
                    .getInputStream()) {
                logoCache = ImageIO.read(stream);
            } catch (Exception ex) {
                BetterQuesting.logger.error(ex);
            }
        }

        if (logoCache != null)
            setIconImage(logoCache);

        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel wholePanel = new JPanel();
        wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));

        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
        add(wholePanel, new JScrollPane(editorPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        JPanel buttonPanel = add(editorPanel, new JPanel());
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), I18n.format("betterquesting.gui.formatting_buttons")));
        addAllFormattingCodes(buttonPanel);

        JPanel textPanel = add(editorPanel, new JPanel());
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        name = add(textPanel, new JTextField(originalName, defaultColumns));
        name.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), I18n.format("betterquesting.gui.name")));
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        UndoHelper.addUndoHelper(name);

        description = add(textPanel, new JTextArea(originalDescription, initialRowCount, defaultColumns));
        description.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), I18n.format("betterquesting.gui.description")));
        description.setLineWrap(true);
        UndoHelper.addUndoHelper(description);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                opened.remove(questID);
            }

        });

        JPanel footerPanel = add(wholePanel, new JPanel());
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton cancel = add(footerPanel, new JButton(I18n.format("gui.cancel")));
        cancel.setMinimumSize(new Dimension(90, 0));
        cancel.addActionListener(this::cancelClicked);
        JButton done = add(footerPanel, new JButton(I18n.format("gui.done")));
        done.addActionListener(this::doneClicked);
        done.setMinimumSize(new Dimension(90, 0));

        setContentPane(wholePanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void cancelClicked(ActionEvent event) {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void doneClicked(ActionEvent event) {
        quest.setProperty(NativeProps.NAME, name.getText().trim());
        quest.setProperty(NativeProps.DESC, description.getText());
        SendChanges();
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void SendChanges() {
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagList dataList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("questID", questID);
        entry.setTag("config", quest.writeToNBT(new NBTTagCompound()));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0);
        NetQuestEdit.sendEdit(payload);
    }

    private void addAllFormattingCodes(JPanel panel) {
        for (TextFormatting value : TextFormatting.values()) {
            JButton btn = new JButton(value.getFriendlyName());
            btn.setToolTipText(value.toString());
            btn.addActionListener(addFormattingCode(value));
            btn.setMinimumSize(new Dimension(80, 20));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            add(panel, btn);
        }
    }

    private ActionListener addFormattingCode(TextFormatting textFormatting) {
        return event -> {
            description.insert(textFormatting.toString(), description.getCaretPosition());
        };
    }

    private static <T extends Component> T add(JPanel panel, T component) {
        panel.add(component);
        return component;
    }

    private static class UndoHelper {

        public static final String ACTION_KEY_UNDO = "undo";
        public static final String ACTION_KEY_REDO = "redo";
        UndoManager undoManager = new UndoManager();

        public static void addUndoHelper(JTextComponent textComponent) {
            new UndoHelper(textComponent);
        }

        private UndoHelper(JTextComponent textComponent) {
            ActionMap amap = textComponent.getActionMap();
            InputMap imap = textComponent.getInputMap();
            if (amap.get(ACTION_KEY_UNDO) == null) {
                UndoAction undoAction = new UndoAction();
                amap.put(ACTION_KEY_UNDO, undoAction);
                imap.put((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY), ACTION_KEY_UNDO);
            }
            if (amap.get(ACTION_KEY_REDO) == null) {
                RedoAction redoAction = new RedoAction();
                amap.put(ACTION_KEY_REDO, redoAction);
                imap.put((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY), ACTION_KEY_REDO);
            }
            textComponent.getDocument().addDocumentListener(new DocListener());
        }

        public UndoManager getUndoManager() { return undoManager; }

        class UndoAction extends AbstractAction {

            UndoAction() {
                super("Undo(U)");
                putValue(MNEMONIC_KEY, (int) 'U');
                putValue(SHORT_DESCRIPTION, "Undo");
                putValue(LONG_DESCRIPTION, "Undo");
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }

        }

        class RedoAction extends AbstractAction {

            RedoAction() {
                super("Redo(R)");
                putValue(MNEMONIC_KEY, (int) 'R');
                putValue(SHORT_DESCRIPTION, "Redo");
                putValue(LONG_DESCRIPTION, "Redo");
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
            }

            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }

        }

        private class DocListener implements DocumentListener {

            public void insertUpdate(DocumentEvent e) {
                if (e instanceof DefaultDocumentEvent) {
                    DefaultDocumentEvent de = (DefaultDocumentEvent) e;
                    undoManager.addEdit(de);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if (e instanceof DefaultDocumentEvent) {
                    DefaultDocumentEvent de = (DefaultDocumentEvent) e;
                    undoManager.addEdit(de);
                }
            }

            public void changedUpdate(DocumentEvent e) {
                // Nothing to do.
            }

        }

    }

    private static class FlashWindowHelper {

        public static void flash(JFrame frame) {
            if (SystemUtils.IS_OS_WINDOWS) {
                FLASHWINFO flashInfo = new FLASHWINFO() {

                    @Override
                    protected List<String> getFieldOrder() { return Arrays.asList("cbSize", "hWnd", "dwFlags", "uCount", "dwTimeout"); }

                };
                flashInfo.cbSize = flashInfo.size();
                flashInfo.hWnd = new HWND(Native.getComponentPointer(frame));
                flashInfo.dwTimeout = 100;
                flashInfo.uCount = 2;
                flashInfo.dwFlags = WinUser.FLASHW_ALL | WinUser.FLASHW_TIMER;

                User32.INSTANCE.FlashWindowEx(flashInfo);
            }
        }

    }

}
