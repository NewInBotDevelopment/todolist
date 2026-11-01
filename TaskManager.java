import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * To-Do List and Task Manager
 * A Java Swing desktop application to add, edit, delete, search,
 * and track tasks. Tasks are saved to a file (tasks.txt) automatically.
 */
public class TaskManager extends JFrame {

    // Colours used across the UI
    private static final Color PRIMARY = new Color(52, 73, 94);
    private static final Color ACCENT = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color WARNING = new Color(230, 126, 34);
    private static final Color LIGHT_BG = new Color(236, 240, 241);

    private static final String DATA_FILE = "tasks.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    // Form fields
    private JTextField titleField;
    private JComboBox<String> priorityBox;
    private JComboBox<String> categoryBox;
    private JTextField dueDateField;
    private JTextArea descriptionArea;

    // Table
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private TableRowSorter<DefaultTableModel> sorter;

    // Search and filter
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    // Status bar labels
    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel completedLabel;

    private int nextId = 1;

    public TaskManager() {
        setTitle("To-Do List and Task Manager");
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(LIGHT_BG);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createFormPanel(), BorderLayout.WEST);
        root.add(createTablePanel(), BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);

        loadTasks();
        if (tableModel.getRowCount() == 0) {
            addSampleTasks();
        }
        updateStats();
    }

    // ---------------------------------------------------------------- UI parts

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("To-Do List and Task Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Organise your work. Track your progress.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(189, 195, 199));

        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        texts.add(title);
        texts.add(subtitle);

        JLabel date = new JLabel("Today: " + DATE_FORMAT.format(new Date()));
        date.setFont(new Font("Segoe UI", Font.BOLD, 14));
        date.setForeground(Color.WHITE);

        header.add(texts, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 10, 0, 0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(ACCENT, 2), " Task Details ",
                                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT),
                        new EmptyBorder(10, 10, 10, 10))));
        form.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.weightx = 1;

        titleField = new JTextField();
        priorityBox = new JComboBox<String>(new String[]{"High", "Medium", "Low"});
        priorityBox.setSelectedIndex(1);
        categoryBox = new JComboBox<String>(new String[]{"Study", "Work", "Personal", "Shopping", "Other"});
        dueDateField = new JTextField(DATE_FORMAT.format(new Date()));
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        int row = 0;
        gc.gridy = row++; form.add(label("Task Title *"), gc);
        gc.gridy = row++; form.add(titleField, gc);
        gc.gridy = row++; form.add(label("Priority"), gc);
        gc.gridy = row++; form.add(priorityBox, gc);
        gc.gridy = row++; form.add(label("Category"), gc);
        gc.gridy = row++; form.add(categoryBox, gc);
        gc.gridy = row++; form.add(label("Due Date (dd-MM-yyyy)"), gc);
        gc.gridy = row++; form.add(dueDateField, gc);
        gc.gridy = row++; form.add(label("Description"), gc);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(250, 80));
        descScroll.setMinimumSize(new Dimension(250, 80));
        gc.gridy = row++; form.add(descScroll, gc);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setOpaque(false);

        JButton addBtn = button("Add Task", SUCCESS);
        JButton updateBtn = button("Update", ACCENT);
        JButton deleteBtn = button("Delete", DANGER);
        JButton doneBtn = button("Mark Done", WARNING);
        JButton clearBtn = button("Clear Form", PRIMARY);
        JButton clearDoneBtn = button("Remove Done", new Color(127, 140, 141));

        addBtn.addActionListener(e -> addTask());
        updateBtn.addActionListener(e -> updateTask());
        deleteBtn.addActionListener(e -> deleteTask());
        doneBtn.addActionListener(e -> toggleDone());
        clearBtn.addActionListener(e -> clearForm());
        clearDoneBtn.addActionListener(e -> removeCompleted());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(doneBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);
        buttons.add(clearDoneBtn);

        gc.gridy = row++;
        gc.insets = new Insets(15, 4, 4, 4);
        form.add(buttons, gc);

        // push everything to the top
        gc.gridy = row;
        gc.weighty = 1;
        form.add(Box.createVerticalGlue(), gc);

        return form;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchField = new JTextField(22);
        statusFilter = new JComboBox<String>(new String[]{"All", "Pending", "Completed"});
        searchBar.add(label("Search:"));
        searchBar.add(searchField);
        searchBar.add(label("Show:"));
        searchBar.add(statusFilter);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        statusFilter.addActionListener(e -> applyFilter());

        // Table
        String[] columns = {"ID", "Title", "Priority", "Category", "Due Date", "Status", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(28);
        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        taskTable.getTableHeader().setBackground(ACCENT);
        taskTable.getTableHeader().setForeground(Color.WHITE);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setSelectionBackground(new Color(174, 214, 241));
        taskTable.setSelectionForeground(Color.BLACK);
        taskTable.setGridColor(new Color(220, 220, 220));

        sorter = new TableRowSorter<DefaultTableModel>(tableModel);
        taskTable.setRowSorter(sorter);

        taskTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        taskTable.getColumnModel().getColumn(6).setPreferredWidth(200);

        taskTable.setDefaultRenderer(Object.class, new TaskCellRenderer());

        // Clicking a row loads it into the form
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadSelectedIntoForm();
                if (e.getClickCount() == 2) {
                    toggleDone();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(taskTable);
        scroll.getViewport().setBackground(Color.WHITE);

        JLabel hint = new JLabel("Tip: click a task to edit it, double-click to mark it done / pending.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(Color.DARK_GRAY);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
        bar.setBackground(PRIMARY);

        totalLabel = statLabel();
        pendingLabel = statLabel();
        completedLabel = statLabel();

        bar.add(totalLabel);
        bar.add(pendingLabel);
        bar.add(completedLabel);
        return bar;
    }

    // ---------------------------------------------------------------- helpers

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(PRIMARY);
        return l;
    }

    private JLabel statLabel() {
        JLabel l = new JLabel();
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 34));
        return b;
    }

    /** Colours rows: green for completed tasks, coloured priority text. */
    private class TaskCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            String status = (String) tableModel.getValueAt(modelRow, 5);
            String priority = (String) tableModel.getValueAt(modelRow, 2);

            if (!isSelected) {
                c.setBackground("Completed".equals(status) ? new Color(212, 239, 223) : Color.WHITE);
                c.setForeground(Color.BLACK);
                if (column == 2) {
                    if ("High".equals(priority)) c.setForeground(DANGER);
                    else if ("Medium".equals(priority)) c.setForeground(WARNING);
                    else c.setForeground(SUCCESS);
                }
            }
            setFont(column == 2 || column == 5 ? getFont().deriveFont(Font.BOLD) : getFont());
            setHorizontalAlignment(column == 0 ? CENTER : LEFT);
            return c;
        }
    }

    // ---------------------------------------------------------------- actions

    private boolean validateForm() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task title.", "Missing Title",
                    JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return false;
        }
        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(dueDateField.getText().trim());
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter the due date as dd-MM-yyyy (e.g. 25-12-2026).",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            dueDateField.requestFocus();
            return false;
        }
        return true;
    }

    private void addTask() {
        if (!validateForm()) return;
        tableModel.addRow(new Object[]{
                nextId++,
                titleField.getText().trim(),
                priorityBox.getSelectedItem(),
                categoryBox.getSelectedItem(),
                dueDateField.getText().trim(),
                "Pending",
                descriptionArea.getText().trim()
        });
        saveTasks();
        updateStats();
        clearForm();
        JOptionPane.showMessageDialog(this, "Task added successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private int selectedModelRow() {
        int viewRow = taskTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task from the table first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return taskTable.convertRowIndexToModel(viewRow);
    }

    private void updateTask() {
        int row = selectedModelRow();
        if (row == -1 || !validateForm()) return;
        tableModel.setValueAt(titleField.getText().trim(), row, 1);
        tableModel.setValueAt(priorityBox.getSelectedItem(), row, 2);
        tableModel.setValueAt(categoryBox.getSelectedItem(), row, 3);
        tableModel.setValueAt(dueDateField.getText().trim(), row, 4);
        tableModel.setValueAt(descriptionArea.getText().trim(), row, 6);
        saveTasks();
        updateStats();
        JOptionPane.showMessageDialog(this, "Task updated successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteTask() {
        int row = selectedModelRow();
        if (row == -1) return;
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete task \"" + tableModel.getValueAt(row, 1) + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
            saveTasks();
            updateStats();
            clearForm();
        }
    }

    private void toggleDone() {
        int row = selectedModelRow();
        if (row == -1) return;
        String status = (String) tableModel.getValueAt(row, 5);
        tableModel.setValueAt("Completed".equals(status) ? "Pending" : "Completed", row, 5);
        saveTasks();
        updateStats();
    }

    private void removeCompleted() {
        int count = 0;
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if ("Completed".equals(tableModel.getValueAt(i, 5))) {
                tableModel.removeRow(i);
                count++;
            }
        }
        saveTasks();
        updateStats();
        clearForm();
        JOptionPane.showMessageDialog(this, count + " completed task(s) removed.", "Done",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSelectedIntoForm() {
        int viewRow = taskTable.getSelectedRow();
        if (viewRow == -1) return;
        int row = taskTable.convertRowIndexToModel(viewRow);
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        priorityBox.setSelectedItem(tableModel.getValueAt(row, 2));
        categoryBox.setSelectedItem(tableModel.getValueAt(row, 3));
        dueDateField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        descriptionArea.setText(String.valueOf(tableModel.getValueAt(row, 6)));
    }

    private void clearForm() {
        titleField.setText("");
        priorityBox.setSelectedIndex(1);
        categoryBox.setSelectedIndex(0);
        dueDateField.setText(DATE_FORMAT.format(new Date()));
        descriptionArea.setText("");
        taskTable.clearSelection();
    }

    private void applyFilter() {
        final String text = searchField.getText().trim().toLowerCase();
        final String status = (String) statusFilter.getSelectedItem();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                boolean statusOk = "All".equals(status) || status.equals(entry.getStringValue(5));
                boolean textOk = text.isEmpty()
                        || entry.getStringValue(1).toLowerCase().contains(text)
                        || entry.getStringValue(3).toLowerCase().contains(text)
                        || entry.getStringValue(6).toLowerCase().contains(text);
                return statusOk && textOk;
            }
        });
    }

    private void updateStats() {
        int total = tableModel.getRowCount();
        int done = 0;
        for (int i = 0; i < total; i++) {
            if ("Completed".equals(tableModel.getValueAt(i, 5))) done++;
        }
        totalLabel.setText("Total Tasks: " + total);
        pendingLabel.setText("Pending: " + (total - done));
        completedLabel.setText("Completed: " + done);
    }

    // ---------------------------------------------------------------- file storage

    private void saveTasks() {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder line = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    if (c > 0) line.append("|");
                    line.append(String.valueOf(tableModel.getValueAt(i, c)).replace("|", "/")
                            .replace("\r", " ").replace("\n", " "));
                }
                out.println(line);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save tasks: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 7) continue;
                int id = Integer.parseInt(p[0]);
                tableModel.addRow(new Object[]{id, p[1], p[2], p[3], p[4], p[5], p[6]});
                nextId = Math.max(nextId, id + 1);
            }
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Could not load tasks: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSampleTasks() {
        tableModel.addRow(new Object[]{nextId++, "Complete Java project", "High", "Study", "15-10-2026", "Pending", "Swing based To-Do app"});
        tableModel.addRow(new Object[]{nextId++, "Prepare for viva", "High", "Study", "16-10-2026", "Pending", "Revise OOP concepts"});
        tableModel.addRow(new Object[]{nextId++, "Buy groceries", "Low", "Shopping", "14-10-2026", "Completed", "Milk, bread, fruits"});
        tableModel.addRow(new Object[]{nextId++, "Team meeting", "Medium", "Work", "18-10-2026", "Pending", "Discuss project progress"});
        saveTasks();
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> new TaskManager().setVisible(true));
    }
}
