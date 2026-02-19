/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Torkild Ulvøy Resheim <torkildr@gmail.com> - initial API and implementation
 */
package net.resheim.sidscore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.DefaultListCellRenderer;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.resheim.sidscore.export.SIDScoreExporter;
import net.resheim.sidscore.ir.RealtimeAudioPlayer;
import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.ir.ScoreBuildingListener;
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.sid.SidModel;

public final class RealtimeAudioPlayerUI {

	private static final int SCOPE_BUFFER = 2048;
	private static final Color C64_BG = new Color(0xD6CDB6);
	private static final Color C64_TEXT = new Color(0xFFFFFF);
	private static final Color C64_ACCENT = new Color(0x6C5EB5);
	private static final Color C64_BUTTON = new Color(0x423724);
	private static final Color C64_NAVIGATOR_TEXT = new Color(0x44c3d26);
	private static final Color SCOPE_BG = new Color(0x4c3d26);
	private static final Color SCOPE_TRACE = new Color(0xD6CDB6);
	private static final Color SIDSCOREAREA_BG = new Color(0x4536ac);
	private static final Color SIDSCOREAREA_TEXT = new Color(0x9a8fff);
	private static final Color MSG_ERROR = new Color(0x68372B);
	private static final Color MSG_WARN = new Color(0x9A6759);
	private static final Color MSG_INFO = SIDSCOREAREA_TEXT;
	private static final Color C64_GRILLE_LIGHT = new Color(0xe1d9cf);
	private static final Color C64_GRILLE_DARK = new Color(0xb39e7d);
	private static final Font C64_FONT = new Font("Monospaced", Font.PLAIN, 13);
	private static final Font C64_FONT_BOLD = new Font("Monospaced", Font.BOLD, 13);
	private static final int BANNER_HEIGHT = 48;
	private static final int GRILLE_LINES = 6;
	private static final int GRILLE_SPACING = 1;
	private static final double SID_CLOCK_NTSC = 1022727.0;
	private static final double SID_CLOCK_PAL = 985248.0;
	private static final double RASTER_RATE_PAL = 50.124542;
	private static final double RASTER_RATE_NTSC = 60.098814;
	private static final long MIN_VICE_LIMIT_CYCLES = 250_000L;
	private static final long MAX_VICE_LIMIT_CYCLES = 2_000_000_000L;
	private static final double VICE_TAIL_SECONDS = 1.5;
	private static final int COMPACT_VICE_CAPTURE_MAX = 12_000;
	private static final int COMPACT_MESSAGES_MAX = 16_000;
	private static volatile boolean compactViceLogs = isCompactLogEnabledByDefault();

	private final JFrame frame = new JFrame("SIDScore Realtime Player");
	private final JTextArea input = new JTextArea(18, 72);
	private final JTextArea messageArea = new JTextArea(6, 72);
	private final JButton copyErrorsButton = new JButton("Copy");
	private final JButton loadButton = new JButton("Load");
	private final JButton playButton = new JButton("Play");
	private final JButton stopButton = new JButton("Stop");
	private final JComboBox<PlaybackRenderer> rendererCombo = new JComboBox<>(PlaybackRenderer.values());
	private final JToggleButton autoReloadButton = new JToggleButton("Auto Reload");
	private final JButton exportAsmButton = new JButton("ASM");
	private final JButton exportWavButton = new JButton("WAV");
	private final JButton exportSidButton = new JButton("SID");
	private final JButton exportPrgButton = new JButton("PRG");
	private final JTree exampleTree = new JTree();
	private final OscilloscopePanel[] scopes = new OscilloscopePanel[3];
	private final BannerPanel bannerPanel = new BannerPanel(resolveBannerPath());
	private final JSplitPane mainSplit;
	private final JSplitPane contentSplit;
	private final JLabel elapsedLabel = new JLabel("00:00");
	private volatile Thread playThread;
	private volatile RealtimeAudioPlayer player;
	private volatile Process viceProcess;
	private volatile boolean viceStopRequested = false;
	private final Timer autoReloadTimer;
	private final Timer clockTimer;
	private final Timer highlightTimer;
	private volatile boolean autoReloadEnabled = false;
	private volatile boolean restartPending = false;
	private volatile boolean restartOnlyWhenAutoReload = false;
	private volatile boolean restartShowDialogs = false;
	private volatile long playbackStartNanos = -1;
	private volatile long playbackStopNanos = -1;
	private volatile double playbackTicksPerSecond = 0.0;
	private volatile List<EventSpan>[] highlightEventsByVoice = null;
	private final int[] highlightIndices = new int[4];
	private final List<Object> playbackHighlightTags = new java.util.ArrayList<>();
	private final Highlighter.HighlightPainter playbackPainter =
			new DefaultHighlighter.DefaultHighlightPainter(C64_ACCENT);
	private SIDScoreParser.FileContext lastParseTree;
	private File lastDirectory;
	private Path currentSourcePath;
	private final Path examplesRoot;

	public static void main(String[] args) {
		compactViceLogs = resolveCompactLogMode(args);
		System.out.println("Working directory: " + Path.of("").toAbsolutePath().normalize());
		SwingUtilities.invokeLater(() -> new RealtimeAudioPlayerUI().show());
	}

	private RealtimeAudioPlayerUI() {
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		controlsPanel.setBackground(C64_BG);
		controlsPanel.add(createSectionLabel("Playback:"));
		controlsPanel.add(rendererCombo);
		controlsPanel.add(loadButton);
		controlsPanel.add(playButton);
		controlsPanel.add(stopButton);
		controlsPanel.add(autoReloadButton);

		JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		exportPanel.setBackground(C64_BG);
		exportPanel.add(createSectionLabel("Export:"));
		exportPanel.add(exportAsmButton);
		exportPanel.add(exportWavButton);
		exportPanel.add(exportSidButton);
		exportPanel.add(exportPrgButton);

		styleButton(loadButton);
		styleButton(playButton);
		styleButton(stopButton);
		styleButton(autoReloadButton);
		styleButton(exportAsmButton);
		styleButton(exportWavButton);
		styleButton(exportSidButton);
		styleButton(exportPrgButton);
		styleComboBox(rendererCombo);

		stopButton.setEnabled(false);

		playButton.addActionListener(e -> onPlay());
		stopButton.addActionListener(e -> onStop(true));
		autoReloadButton.addActionListener(e -> onAutoReloadToggle());
		loadButton.addActionListener(e -> onLoad());
		exportAsmButton.addActionListener(e -> onExport(ExportFormat.ASM));
		exportWavButton.addActionListener(e -> onExport(ExportFormat.WAV));
		exportSidButton.addActionListener(e -> onExport(ExportFormat.SID));
		exportPrgButton.addActionListener(e -> onExport(ExportFormat.PRG));

		JScrollPane editorScroll = new JScrollPane(input);
		styleScrollPane(editorScroll);
		editorScroll.getViewport().setBackground(SIDSCOREAREA_BG);
		editorScroll.setBackground(SIDSCOREAREA_BG);
		input.setFont(C64_FONT);
		input.setBackground(SIDSCOREAREA_BG);
		input.setForeground(SIDSCOREAREA_TEXT);
		input.setCaretColor(SIDSCOREAREA_TEXT);
		input.setSelectionColor(C64_ACCENT);
		input.setSelectedTextColor(SIDSCOREAREA_TEXT);
		input.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				onSourceChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				onSourceChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onSourceChanged();
			}
		});

		JPanel scopeStack = new JPanel();
		scopeStack.setLayout(new javax.swing.BoxLayout(scopeStack, javax.swing.BoxLayout.Y_AXIS));
		scopeStack.setBackground(C64_BG);
		for (int i = 0; i < scopes.length; i++) {
			int voiceIndex = i + 1;
			OscilloscopePanel scope = new OscilloscopePanel("Voice " + voiceIndex);
			scope.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			scopes[i] = scope;
			scopeStack.add(scope);
		}
		JPanel scopeFooter = new JPanel(new BorderLayout());
		scopeFooter.setBackground(SCOPE_BG);
		scopeFooter.setOpaque(true);
		elapsedLabel.setFont(C64_FONT_BOLD);
		elapsedLabel.setForeground(SCOPE_TRACE);
		elapsedLabel.setBackground(SCOPE_BG);
		elapsedLabel.setOpaque(true);
		elapsedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		scopeFooter.add(elapsedLabel, BorderLayout.CENTER);
		scopeFooter.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		scopeStack.add(scopeFooter);

		JPanel editorPanel = wrapWithLabel("SIDScore", editorScroll);

		JScrollPane examplesScroll = new JScrollPane(exampleTree);
		styleScrollPane(examplesScroll);
		examplesScroll.setBackground(C64_BG);
		examplesScroll.getViewport().setBackground(C64_BG);
		exampleTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		exampleTree.setRootVisible(false);
		// exampleTree.setFont(C64_FONT);
		exampleTree.setBackground(C64_BG);
		exampleTree.setForeground(C64_NAVIGATOR_TEXT);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setTextNonSelectionColor(C64_NAVIGATOR_TEXT);
		renderer.setTextSelectionColor(C64_BG);
		renderer.setBackgroundNonSelectionColor(C64_BG);
		renderer.setBackgroundSelectionColor(C64_ACCENT);
		renderer.setBorderSelectionColor(C64_ACCENT);
		// renderer.setFont(C64_FONT);
		exampleTree.setCellRenderer(renderer);
		exampleTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					handleTreeActivate();
				}
			}
		});
		exampleTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "activateSelected");
		exampleTree.getActionMap().put("activateSelected", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				handleTreeActivate();
			}
		});

		JPanel examplesPanel = wrapWithLabel("Examples", examplesScroll);
		// examplesPanel.setBorder(BorderFactory.createEmptyBorder());
		JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, examplesPanel, scopeStack);
		leftSplit.setResizeWeight(0.6);
		styleSplitPane(leftSplit, 6);

		mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, editorPanel);
		mainSplit.setResizeWeight(0.3);
		styleSplitPane(mainSplit, 6);

		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setFont(C64_FONT);
		messageArea.setForeground(SCOPE_BG);
		messageArea.setBackground(C64_BG);
		JScrollPane messageScroll = new JScrollPane(messageArea);
		styleScrollPane(messageScroll);
		messageScroll.setBackground(C64_BG);
		messageScroll.getViewport().setBackground(C64_BG);
		styleButton(copyErrorsButton);
		copyErrorsButton.addActionListener(e -> copyErrorMessages());
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBackground(C64_BG);
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		JPanel messageHeader = new JPanel(new BorderLayout());
		messageHeader.setBackground(C64_BG);
		JLabel messageLabel = new JLabel("Messages");
		messageLabel.setFont(C64_FONT_BOLD);
		messageLabel.setForeground(C64_NAVIGATOR_TEXT);
		messageHeader.add(messageLabel, BorderLayout.WEST);
		messageHeader.add(copyErrorsButton, BorderLayout.EAST);
		messagePanel.add(messageHeader, BorderLayout.NORTH);
		messagePanel.add(messageScroll, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout(8, 8));
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(C64_BG);
		header.add(bannerPanel, BorderLayout.NORTH);
		JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
		headerControls.setBackground(C64_BG);
		headerControls.add(controlsPanel);
		headerControls.add(exportPanel);
		header.add(headerControls, BorderLayout.SOUTH);
		GrillePanel grillePanel = new GrillePanel();
		JPanel headerWrapper = new JPanel(new BorderLayout());
		headerWrapper.setBackground(C64_BG);
		headerWrapper.setBorder(null);
		headerWrapper.add(header, BorderLayout.NORTH);
		headerWrapper.add(grillePanel, BorderLayout.SOUTH);
		frame.add(headerWrapper, BorderLayout.NORTH);
		contentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainSplit, messagePanel);
		contentSplit.setResizeWeight(0.85);
		styleSplitPane(contentSplit, 6);

		frame.add(contentSplit, BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(780, 500));
		frame.getContentPane().setBackground(C64_BG);

		Timer repaintTimer = new Timer(16, e -> {
			for (OscilloscopePanel scope : scopes) {
				scope.repaint();
			}
		});
		repaintTimer.start();

		autoReloadTimer = new Timer(250, e -> requestAutoRestart());
		autoReloadTimer.setRepeats(false);
		clockTimer = new Timer(500, e -> updateElapsedClock());
		clockTimer.start();
		highlightTimer = new Timer(60, e -> updatePlaybackHighlight());
		highlightTimer.start();

		examplesRoot = resolveExamplesRoot();
		File defaultFile = examplesRoot.toFile();
		lastDirectory = defaultFile.isDirectory() ? defaultFile : new File(System.getProperty("user.dir"));

		refreshExampleList();
	}

	private void show() {
		frame.pack();
		frame.setSize(frame.getWidth(), Math.max(500, Math.round(frame.getHeight() * 0.65f)) - 25);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		SwingUtilities.invokeLater(() -> {
			mainSplit.setDividerLocation(0.3);
			contentSplit.setDividerLocation(0.76);
		});
	}

	private void onPlay() {
		if (isPlaying()) {
			onStop(true);
			return;
		}
		startPlayback(true);
	}

	private void onStop(boolean clearRestart) {
		if (clearRestart) {
			restartPending = false;
			restartOnlyWhenAutoReload = false;
		}
		viceStopRequested = true;
		if (playbackStartNanos > 0 && playbackStopNanos < 0) {
			playbackStopNanos = System.nanoTime();
		}
		resetPlaybackHighlighting();
		Process renderProcess = viceProcess;
		if (renderProcess != null) {
			renderProcess.destroy();
			if (renderProcess.isAlive()) {
				renderProcess.destroyForcibly();
			}
		}
		RealtimeAudioPlayer current = player;
		if (current != null) {
			current.stop();
		}
		updatePlaybackButtons();
	}

	private void onAutoReloadToggle() {
		autoReloadEnabled = autoReloadButton.isSelected();
		if (autoReloadEnabled) {
			requestAutoRestart();
		} else {
			restartPending = false;
			restartOnlyWhenAutoReload = false;
			autoReloadTimer.stop();
		}
	}

	private void onLoad() {
		JFileChooser chooser = new JFileChooser(lastDirectory);
		chooser.setFileFilter(new FileNameExtensionFilter("SIDScore files (*.sidscore)", "sidscore"));
		int result = chooser.showOpenDialog(frame);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File selected = chooser.getSelectedFile();
		lastDirectory = selected.getParentFile();
		currentSourcePath = selected.toPath();
		try {
			String text = Files.readString(selected.toPath());
			input.setText(text);
			input.setCaretPosition(0);
			setMessage("Loaded: " + selected.getName(), MSG_INFO);
		} catch (IOException ex) {
			String msg = "Failed to load: " + selected.getName() + " (" + ex.getMessage() + ")";
			setMessage(msg, MSG_ERROR);
			JOptionPane.showMessageDialog(frame, msg, "Load Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onExport(ExportFormat format) {
		SIDScoreIR.TimedScore timed = parseScore(input.getText(), true, true);
		if (timed == null) {
			return;
		}
		Path outPath = chooseExportPath(format);
		if (outPath == null) {
			return;
		}
		setMessage("Exporting " + format.label + "...", MSG_INFO);
		Thread exportThread = new Thread(() -> {
			try {
				exportScore(format, timed, outPath);
				SwingUtilities.invokeLater(
						() -> setMessage("Exported " + format.label + ": " + outPath.getFileName(), MSG_INFO));
			} catch (Exception ex) {
				SwingUtilities.invokeLater(() -> {
					String msg = "Export failed: " + ex.getMessage();
					setMessage(msg, MSG_ERROR);
					JOptionPane.showMessageDialog(frame, msg, "Export Error", JOptionPane.ERROR_MESSAGE);
				});
			}
		}, "sidscore-export-" + format.label.toLowerCase());
		exportThread.setDaemon(true);
		exportThread.start();
	}

	private void exportScore(ExportFormat format, SIDScoreIR.TimedScore timed, Path outPath)
			throws IOException, InterruptedException {
		SIDScoreExporter exporter = new SIDScoreExporter();
		switch (format) {
			case ASM -> {
				deleteIfExists(outPath);
				exporter.writeAsm(timed, outPath, true);
			}
			case PRG -> {
				Path asmPath = withExtension(outPath, ".asm");
				deleteIfExists(asmPath);
				exporter.writeAsm(timed, asmPath, true);
				deleteIfExists(outPath);
				exporter.assemble(asmPath, outPath);
			}
			case SID -> {
				Path asmPath = withExtension(outPath, ".asm");
				Path prgPath = withExtension(outPath, ".prg");
				deleteIfExists(asmPath);
				exporter.writeAsm(timed, asmPath, false);
				deleteIfExists(prgPath);
				exporter.assemble(asmPath, prgPath);
				deleteIfExists(outPath);
				exporter.writeSid(prgPath, timed, outPath, SidModel.MOS6581);
			}
			case WAV -> {
				deleteIfExists(outPath);
				new RealtimeAudioPlayer().renderToWav(timed, outPath);
			}
		}
	}

	private Path chooseExportPath(ExportFormat format) {
		JFileChooser chooser = new JFileChooser(lastDirectory);
		chooser.setDialogTitle("Export " + format.label);
		String ext = format.extension;
		chooser.setFileFilter(new FileNameExtensionFilter(format.label + " files (*" + ext + ")", ext.substring(1)));
		chooser.setSelectedFile(defaultExportPath(ext).toFile());
		int result = chooser.showSaveDialog(frame);
		if (result != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File selected = chooser.getSelectedFile();
		if (selected == null) {
			return null;
		}
		lastDirectory = selected.getParentFile();
		return ensureExtension(selected.toPath(), ext);
	}

	private Path defaultExportPath(String extension) {
		Path base = currentSourcePath != null ? currentSourcePath
				: (lastDirectory != null ? lastDirectory.toPath().resolve("sidscore-export") : Path.of("sidscore-export"));
		Path dir = base.getParent() != null ? base.getParent()
				: (lastDirectory != null ? lastDirectory.toPath() : Path.of("."));
		String name = base.getFileName().toString();
		int dot = name.lastIndexOf('.');
		if (dot >= 0) {
			name = name.substring(0, dot);
		}
		return dir.resolve(name + extension);
	}

	private static Path ensureExtension(Path path, String extension) {
		String name = path.getFileName().toString();
		if (name.toLowerCase().endsWith(extension)) {
			return path;
		}
		String updated = name + extension;
		return path.getParent() == null ? Path.of(updated) : path.getParent().resolve(updated);
	}

	private static Path withExtension(Path path, String extension) {
		String name = path.getFileName().toString();
		int dot = name.lastIndexOf('.');
		String base = dot >= 0 ? name.substring(0, dot) : name;
		Path parent = path.getParent();
		String updated = base + extension;
		return parent == null ? Path.of(updated) : parent.resolve(updated);
	}

	private static void deleteIfExists(Path path) throws IOException {
		if (path == null) {
			return;
		}
		if (Files.exists(path)) {
			if (Files.isDirectory(path)) {
				throw new IOException("Output path is a directory: " + path);
			}
			Files.delete(path);
		}
	}

	private void onExampleDoubleClick(ExampleEntry entry) {
		try {
			currentSourcePath = entry.path;
			String text = Files.readString(entry.path);
			input.setText(text);
			input.setCaretPosition(0);
			setMessage("Loaded: " + entry.label, MSG_INFO);
			if (isPlaying()) {
				restartPending = true;
				restartOnlyWhenAutoReload = false;
				restartShowDialogs = true;
				onStop(false);
			} else {
				startPlayback(true);
			}
		} catch (IOException ex) {
			String msg = "Failed to load: " + entry.label + " (" + ex.getMessage() + ")";
			setMessage(msg, MSG_ERROR);
			JOptionPane.showMessageDialog(frame, msg, "Load Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void refreshExampleList() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Examples");
		if (Files.isDirectory(examplesRoot)) {
			try (var stream = Files.walk(examplesRoot)) {
				stream.filter(p -> Files.isRegularFile(p))
						.filter(p -> p.toString().endsWith(".sidscore"))
						.sorted(Comparator.comparing(Path::toString))
						.forEach(p -> addToTree(root, p));
			} catch (IOException e) {
				setMessage("Failed to read examples: " + e.getMessage(), MSG_WARN);
			}
		} else {
			setMessage("Examples folder not found: " + examplesRoot, MSG_WARN);
		}
		DefaultTreeModel model = new DefaultTreeModel(root);
		exampleTree.setModel(model);
		collapseAll();
	}

	private String toDisplayLabel(Path path) {
		Metadata meta = readMetadata(path);
		Path rel = examplesRoot.relativize(path);
		String fallback = rel.toString().replace(File.separatorChar, '/');
		if (meta.title == null && meta.author == null) {
			return fallback;
		}
		if (meta.title != null && meta.author != null) {
			return meta.title + " — " + meta.author;
		}
		return meta.title != null ? meta.title : meta.author;
	}

	private void addToTree(DefaultMutableTreeNode root, Path path) {
		Path rel = examplesRoot.relativize(path);
		DefaultMutableTreeNode parent = root;
		for (int i = 0; i < rel.getNameCount(); i++) {
			String name = rel.getName(i).toString();
			boolean isLeaf = i == rel.getNameCount() - 1;
			if (isLeaf) {
				parent.add(new DefaultMutableTreeNode(new ExampleEntry(path, toDisplayLabel(path)), false));
			} else {
				DefaultMutableTreeNode child = findChild(parent, name);
				if (child == null) {
					child = new DefaultMutableTreeNode(name, true);
					parent.add(child);
				}
				parent = child;
			}
		}
	}

	private DefaultMutableTreeNode findChild(DefaultMutableTreeNode parent, String name) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			Object user = ((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject();
			if (user instanceof String s && s.equals(name)) {
				return (DefaultMutableTreeNode) parent.getChildAt(i);
			}
		}
		return null;
	}

	private void collapseAll() {
		for (int i = exampleTree.getRowCount() - 1; i >= 0; i--) {
			exampleTree.collapseRow(i);
		}
	}

	private void handleTreeActivate() {
		TreePath path = exampleTree.getSelectionPath();
		if (path == null) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object user = node.getUserObject();
		if (user instanceof ExampleEntry entry) {
			onExampleDoubleClick(entry);
			return;
		}
		if (exampleTree.isExpanded(path)) {
			exampleTree.collapsePath(path);
		} else {
			exampleTree.expandPath(path);
		}
	}

	private Metadata readMetadata(Path path) {
		String title = null;
		String author = null;
		try {
			List<String> lines = Files.readAllLines(path);
			for (String line : lines) {
				String trimmed = line.trim();
				if (trimmed.startsWith("TITLE ")) {
					title = parseQuoted(trimmed);
				} else if (trimmed.startsWith("AUTHOR ")) {
					author = parseQuoted(trimmed);
				}
				if (title != null && author != null) {
					break;
				}
			}
		} catch (IOException e) {
			return new Metadata(null, null);
		}
		return new Metadata(title, author);
	}

	private String parseQuoted(String line) {
		int first = line.indexOf('\"');
		int last = line.lastIndexOf('\"');
		if (first >= 0 && last > first) {
			return line.substring(first + 1, last);
		}
		return null;
	}

	private static Path resolveExamplesRoot() {
		Path current = Path.of("").toAbsolutePath().normalize();
		for (int i = 0; i < 4 && current != null; i++) {
			Path candidate = current.resolve("examples");
			if (Files.isDirectory(candidate)) {
				Path sfx = candidate.resolve("sfx");
				Path melodies = candidate.resolve("melodies");
				if (Files.isDirectory(sfx) || Files.isDirectory(melodies)) {
					return candidate;
				}
			}
			current = current.getParent();
		}
		return Path.of("examples");
	}

	private static Path resolveBannerPath() {
		Path current = Path.of("").toAbsolutePath().normalize();
		for (int i = 0; i < 4 && current != null; i++) {
			Path candidate = current.resolve("docs").resolve("banner.png");
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
			current = current.getParent();
		}
		return Path.of("docs", "banner.png");
	}

	private static void styleButton(javax.swing.AbstractButton button) {
		button.setFont(C64_FONT_BOLD);
		button.setBackground(C64_BUTTON);
	}

	private static void styleComboBox(JComboBox<?> combo) {
		combo.setFont(C64_FONT_BOLD);
		combo.setForeground(C64_BUTTON);
		combo.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -1849548976203434835L;

			@Override
			public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				label.setFont(C64_FONT_BOLD);
				label.setOpaque(true);
				return label;
			}
		});
	}

	private static void styleScrollPane(JScrollPane pane) {
		pane.setBorder(BorderFactory.createEmptyBorder());
	}

	private static void styleSplitPane(JSplitPane splitPane, int dividerSize) {
		splitPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		splitPane.setBackground(C64_BG);
		splitPane.setUI(new BasicSplitPaneUI() {
			@Override
			public BasicSplitPaneDivider createDefaultDivider() {
				BasicSplitPaneDivider divider = super.createDefaultDivider();
				divider.setBackground(C64_BG);
				return divider;
			}
		});
		splitPane.setDividerSize(dividerSize);
	}

	private static JLabel createSectionLabel(String title) {
		JLabel label = new JLabel(title);
		label.setFont(UIManager.getFont("Label.font"));
		label.setForeground(C64_NAVIGATOR_TEXT);
		return label;
	}

	private static JPanel wrapWithLabel(String title, JComponent content) {
		JLabel label = new JLabel(title);
		label.setFont(C64_FONT_BOLD);
		label.setForeground(C64_NAVIGATOR_TEXT);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(C64_BG);
		panel.add(label, BorderLayout.NORTH);
		panel.add(content, BorderLayout.CENTER);
		return panel;
	}

	private void onSourceChanged() {
		if (!autoReloadEnabled) {
			return;
		}
		autoReloadTimer.restart();
	}

	private boolean isPlaying() {
		return playThread != null && playThread.isAlive();
	}

	private void setPlaybackButtons(boolean playing) {
		playButton.setEnabled(!playing);
		stopButton.setEnabled(playing);
		rendererCombo.setEnabled(!playing);
	}

	private void updatePlaybackButtons() {
		setPlaybackButtons(isPlaying());
	}

	private boolean markPlaybackStartIfUnset() {
		synchronized (this) {
			if (playbackStartNanos > 0) {
				return false;
			}
			playbackStartNanos = System.nanoTime();
			playbackStopNanos = -1;
		}
		SwingUtilities.invokeLater(this::updateElapsedClock);
		return true;
	}

	private void updateElapsedClock() {
		long start = playbackStartNanos;
		if (start <= 0) {
			elapsedLabel.setText("00:00");
			return;
		}
		long end = playbackStopNanos > 0 ? playbackStopNanos : System.nanoTime();
		long elapsedSeconds = (end - start) / 1_000_000_000L;
		elapsedLabel.setText(formatElapsed(elapsedSeconds));
	}

	private static String formatElapsed(long seconds) {
		long hours = seconds / 3600;
		long mins = (seconds % 3600) / 60;
		long secs = seconds % 60;
		if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, mins, secs);
		}
		return String.format("%02d:%02d", mins, secs);
	}

	private void requestAutoRestart() {
		restartPending = true;
		restartOnlyWhenAutoReload = true;
		restartShowDialogs = false;
		if (!isPlaying()) {
			restartPending = false;
			restartOnlyWhenAutoReload = false;
			startPlayback(false);
		} else {
			onStop(false);
		}
	}

	private boolean startPlayback(boolean showDialogs) {
		resetPlaybackHighlighting();
		SIDScoreIR.TimedScore timed = parseScore(input.getText(), showDialogs, showDialogs);
		if (timed == null) {
			return false;
		}
		for (OscilloscopePanel scope : scopes) {
			scope.clear();
		}

		PlaybackRenderer renderer = (PlaybackRenderer) rendererCombo.getSelectedItem();
		if (renderer == null) {
			renderer = PlaybackRenderer.SRAP;
		}
		prepareHighlighting(timed, renderer == PlaybackRenderer.VICE ? HighlightTimeline.FRAMES : HighlightTimeline.TICKS);
		setPlaybackButtons(true);

		Thread thread;
		if (renderer == PlaybackRenderer.VICE) {
			thread = new Thread(() -> runVicePlayback(timed), "sidscore-vice-player");
		} else {
			thread = new Thread(() -> runSrapPlayback(timed), "sidscore-realtime-player");
		}
		thread.setDaemon(true);
		playThread = thread;
		playThread.start();
		return true;
	}

	private void runSrapPlayback(SIDScoreIR.TimedScore timed) {
		playbackStartNanos = -1;
		playbackStopNanos = -1;
		viceStopRequested = false;
		SwingUtilities.invokeLater(this::updateElapsedClock);
		java.util.concurrent.atomic.AtomicBoolean startMarked = new java.util.concurrent.atomic.AtomicBoolean(false);

		RealtimeAudioPlayer currentPlayer = new RealtimeAudioPlayer();
		player = currentPlayer;
		try {
			currentPlayer.play(timed, (v1, v2, v3, length, sampleRate) -> {
				if (startMarked.compareAndSet(false, true)) {
					markPlaybackStartIfUnset();
				}
				scopes[0].append(v1, length);
				scopes[1].append(v2, length);
				scopes[2].append(v3, length);
			});
		} catch (LineUnavailableException e) {
			SwingUtilities.invokeLater(
					() -> JOptionPane.showMessageDialog(frame, e.getMessage(), "Audio Error",
							JOptionPane.ERROR_MESSAGE));
		} finally {
			if (player == currentPlayer) {
				player = null;
			}
			markPlaybackStartIfUnset();
			finishPlayback(Thread.currentThread());
		}
	}

	private void runVicePlayback(SIDScoreIR.TimedScore timed) {
		viceStopRequested = false;
		playbackStartNanos = -1;
		playbackStopNanos = -1;
		SwingUtilities.invokeLater(this::updateElapsedClock);

		Path tempDir = null;
		try {
			appendMessageAsync("VICE: generating SID...", MSG_INFO);
			tempDir = Files.createTempDirectory("sidscore-vice-");
			Path asmPath = tempDir.resolve("preview.asm");
			Path prgPath = tempDir.resolve("preview.prg");
			Path sidPath = tempDir.resolve("preview.sid");

			SIDScoreExporter exporter = new SIDScoreExporter();
			exporter.writeAsm(timed, asmPath, false);
			exporter.assemble(asmPath, prgPath);
			exporter.writeSid(prgPath, timed, sidPath, SidModel.MOS6581);
			appendCompiledProgramStatsForVice(timed, exporter, prgPath, sidPath);

			appendMessageAsync("VICE: direct playback...", MSG_INFO);
			playWithViceDirect(sidPath, timed);
			if (viceStopRequested) {
				return;
			}
			appendMessageAsync("VICE: playback complete.", MSG_INFO);
		} catch (Exception ex) {
			if (!viceStopRequested) {
				String msg = "Playback failed: " + ex.getMessage();
				appendMessageAsync(msg, MSG_ERROR);
			}
		} finally {
			viceProcess = null;
			deleteRecursively(tempDir);
			finishPlayback(Thread.currentThread());
		}
	}

	private void finishPlayback(Thread ownerThread) {
		SwingUtilities.invokeLater(() -> {
			if (playThread != ownerThread) {
				return;
			}
			setPlaybackButtons(false);
			playThread = null;
			if (playbackStartNanos > 0 && playbackStopNanos < 0) {
				playbackStopNanos = System.nanoTime();
			}
			resetPlaybackHighlighting();
			updateElapsedClock();
			if (restartPending && (!restartOnlyWhenAutoReload || autoReloadEnabled)) {
				boolean show = restartShowDialogs;
				restartPending = false;
				restartOnlyWhenAutoReload = false;
				startPlayback(show);
			}
		});
	}

	private void playWithViceDirect(Path sidPath, SIDScoreIR.TimedScore timed)
			throws IOException, InterruptedException {
		String viceBinary = resolveViceBinary();
		long limitCycles = estimateLimitCycles(timed);
		long timeoutMillis = estimateViceTimeoutMillis(limitCycles, timed.system());

		List<String> cmd = new java.util.ArrayList<>();
		cmd.add(viceBinary);
		cmd.add("-default");
		String viceDataDir = resolveViceDataDir(viceBinary);
		if (viceDataDir != null) {
			cmd.add("-directory");
			cmd.add(viceDataDir);
		}
		if (compactViceLogs) {
			cmd.add("-silent");
		} else {
			cmd.add("-verbose");
		}
		cmd.add("-console");
		cmd.add("-sound");
		cmd.add("-sounddev");
		cmd.add("coreaudio");
		cmd.add("-soundrate");
		cmd.add("44100");
		cmd.add("-sidmodel");
		cmd.add("0");
		cmd.add(timed.system() == SIDScoreIR.VideoSystem.NTSC ? "-ntsc" : "-pal");
		cmd.add("-limitcycles");
		cmd.add(Long.toString(limitCycles));
		cmd.add(sidPath.toString());

		appendMessageAsync("VICE command: " + String.join(" ", cmd), MSG_INFO);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		viceProcess = process;
		playbackStartNanos = -1;
		playbackStopNanos = -1;
		SwingUtilities.invokeLater(this::updateElapsedClock);

		java.util.concurrent.atomic.AtomicBoolean startMarked = new java.util.concurrent.atomic.AtomicBoolean(false);
		java.util.concurrent.atomic.AtomicBoolean soundDeviceOpened = new java.util.concurrent.atomic.AtomicBoolean(false);
		Thread startFallback = new Thread(() -> {
			try {
				Thread.sleep(1200);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
			if (!viceStopRequested && process.isAlive() && startMarked.compareAndSet(false, true)) {
				markPlaybackStartIfUnset();
			}
		}, "sidscore-vice-start-fallback");
		startFallback.setDaemon(true);
		startFallback.start();

		StringBuffer viceLog = new StringBuffer();
		Thread logThread = new Thread(() -> {
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					appendMessageAsync("vsid> " + line, MSG_INFO);
					String lower = line.toLowerCase(Locale.ROOT);
					if (lower.contains("sound: opened device")) {
						soundDeviceOpened.set(true);
					}
					if (soundDeviceOpened.get() && lower.contains("vsync: sync reset")
							&& startMarked.compareAndSet(false, true)) {
						markPlaybackStartIfUnset();
					}
					if (!compactViceLogs || viceLog.length() < COMPACT_VICE_CAPTURE_MAX) {
						viceLog.append(line).append('\n');
					}
				}
			} catch (IOException e) {
				appendMessageAsync("VICE log read error: " + e.getMessage(), MSG_WARN);
			}
		}, "sidscore-vice-log");
		logThread.setDaemon(true);
		logThread.start();

		boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
		if (!finished) {
			appendMessageAsync("VICE timeout after " + (timeoutMillis / 1000) + "s. Terminating process.", MSG_WARN);
			process.destroy();
			if (process.isAlive()) {
				process.destroyForcibly();
			}
			logThread.join(1000);
			throw new IOException("VICE timed out while rendering audio.");
		}

		logThread.join(1000);
		startFallback.join(1000);
		int code = process.exitValue();
		viceProcess = null;
		if (viceStopRequested) {
			return;
		}
		markPlaybackStartIfUnset();
		String text = viceLog.toString().trim();
		boolean cycleLimitReached = text.toLowerCase(Locale.ROOT).contains("cycle limit reached");
		if (code != 0) {
			if (cycleLimitReached) {
				appendMessageAsync("VICE: cycle limit reached (expected end of preview).", MSG_WARN);
				return;
			}
			String sanitized = sanitizeViceErrorLog(text);
			if (sanitized.isBlank()) {
				throw new IOException("VICE exited with code " + code);
			}
			throw new IOException("VICE exited with code " + code + ": " + sanitized);
		}
	}

	private static long estimateLimitCycles(SIDScoreIR.TimedScore timed) {
		int maxTicks = 0;
		for (int voice = 1; voice <= 3; voice++) {
			SIDScoreIR.TimedVoice tv = timed.voices().get(voice);
			if (tv == null) {
				continue;
			}
			int ticks = tv.events().stream().mapToInt(SIDScoreIR.TimedEvent::durationTicks).sum();
			if (ticks > maxTicks) {
				maxTicks = ticks;
			}
		}
		double ticksPerSecond = timed.ticksPerWhole() / 4.0 * timed.tempoBpm() / 60.0;
		if (ticksPerSecond <= 0.0) {
			return MIN_VICE_LIMIT_CYCLES;
		}
		double durationSeconds = (maxTicks / ticksPerSecond) + VICE_TAIL_SECONDS;
		double clock = timed.system() == SIDScoreIR.VideoSystem.NTSC ? SID_CLOCK_NTSC : SID_CLOCK_PAL;
		long cycles = (long) Math.ceil(durationSeconds * clock);
		if (cycles < MIN_VICE_LIMIT_CYCLES) {
			return MIN_VICE_LIMIT_CYCLES;
		}
		if (cycles > MAX_VICE_LIMIT_CYCLES) {
			return MAX_VICE_LIMIT_CYCLES;
		}
		return cycles;
	}

	private static long estimateViceTimeoutMillis(long limitCycles, SIDScoreIR.VideoSystem system) {
		double clock = system == SIDScoreIR.VideoSystem.NTSC ? SID_CLOCK_NTSC : SID_CLOCK_PAL;
		double emulatedSeconds = limitCycles / clock;
		long ms = (long) Math.ceil((emulatedSeconds + 10.0) * 1000.0);
		long min = 15_000L;
		long max = 5 * 60 * 1000L;
		if (ms < min) {
			return min;
		}
		if (ms > max) {
			return max;
		}
		return ms;
	}

	private static String resolveViceBinary() {
		String override = System.getenv("SIDSCORE_VICE_BIN");
		if (override != null && !override.isBlank()) {
			return override.trim();
		}
		return "vsid";
	}

	private static boolean resolveCompactLogMode(String[] args) {
		boolean mode = isCompactLogEnabledByDefault();
		if (args == null) {
			return mode;
		}
		for (String arg : args) {
			if ("--compact-vice-log".equals(arg)) {
				mode = true;
			} else if ("--full-vice-log".equals(arg)) {
				mode = false;
			}
		}
		return mode;
	}

	private static boolean isCompactLogEnabledByDefault() {
		String env = System.getenv("SIDSCORE_VICE_COMPACT_LOG");
		if (isTruthy(env)) {
			return true;
		}
		return Boolean.getBoolean("sidscore.vice.compactLog");
	}

	private static boolean isTruthy(String value) {
		if (value == null) {
			return false;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return "1".equals(normalized) || "true".equals(normalized) || "yes".equals(normalized)
				|| "on".equals(normalized);
	}

	private static String resolveViceDataDir(String viceBinary) {
		String override = System.getenv("SIDSCORE_VICE_DATA_DIR");
		if (override != null && !override.isBlank()) {
			return override.trim();
		}
		try {
			Path binaryPath = Path.of(viceBinary);
			if (!binaryPath.isAbsolute()) {
				return null;
			}
			Path real = binaryPath.toRealPath();
			Path cellarRoot = real.getParent() != null && real.getParent().getParent() != null
					? real.getParent().getParent()
					: null;
			if (cellarRoot == null) {
				return null;
			}
			Path candidate = cellarRoot.resolve("share").resolve("vice");
			if (Files.isDirectory(candidate)) {
				return candidate.toString();
			}
		} catch (Exception ignored) {
			// best effort only
		}
		return null;
	}

	private void appendCompiledProgramStatsForVice(SIDScoreIR.TimedScore timed,
			SIDScoreExporter exporter,
			Path prgPath,
			Path sidPath) throws IOException {
		long prgSize = Files.size(prgPath);
		if (prgSize < 2) {
			return;
		}
		byte[] header = Files.readAllBytes(prgPath);
		int loadAddress = (header[0] & 0xFF) | ((header[1] & 0xFF) << 8);
		long imageBytes = prgSize - 2;
		SIDScoreExporter.ProgramStats stats = exporter.estimateProgramStats(timed);
		long scoreBytes = stats.scoreBytes();
		long driverBytes = Math.max(0L, imageBytes - scoreBytes);

		appendMessageAsync("Compiled With Driver: sidscore", MSG_INFO);
		appendMessageAsync("Program Size: " + imageBytes + " bytes (load $" + hex4(loadAddress) + ")", MSG_INFO);
		appendMessageAsync("Size Split: driver~" + driverBytes + " bytes, score~" + scoreBytes + " bytes", MSG_INFO);
		appendMessageAsync("Score Data: voice-events=" + stats.voiceEventBytes() + " bytes, tables="
				+ stats.tableBytes() + " bytes", MSG_INFO);
		appendMessageAsync("Driver Data: note-freq-table=" + stats.noteFreqTableBytes() + " bytes", MSG_INFO);
		if (Files.exists(sidPath)) {
			appendMessageAsync("SID Size: " + Files.size(sidPath) + " bytes", MSG_INFO);
		}
	}

	private static String hex4(int v) {
		return String.format("%04x", v & 0xFFFF);
	}

	private static String sanitizeViceErrorLog(String raw) {
		if (raw == null || raw.isBlank()) {
			return "";
		}
		if (!compactViceLogs) {
			return raw.trim();
		}
		StringBuilder sb = new StringBuilder();
		for (String line : raw.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			String lower = trimmed.toLowerCase(Locale.ROOT);
			if (lower.contains("failed to retrieve executable path")
					|| lower.contains("invalid or unset autosave screenshot format")) {
				continue;
			}
			sb.append(trimmed).append('\n');
		}
		return sb.toString().trim();
	}

	private static void deleteRecursively(Path root) {
		if (root == null || !Files.exists(root)) {
			return;
		}
		try (var stream = Files.walk(root)) {
			stream.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
					// best effort cleanup
				}
			});
		} catch (IOException ignored) {
			// best effort cleanup
		}
	}

	private SIDScoreIR.TimedScore parseScore(String src, boolean showErrors, boolean showWarnings) {
		try {
			SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(src));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SIDScoreParser parser = new SIDScoreParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener());

			SIDScoreParser.FileContext tree = parser.file();
			lastParseTree = tree;
			Path importBase = currentSourcePath != null ? currentSourcePath
					: (lastDirectory != null ? lastDirectory.toPath() : null);
			ScoreBuildingListener builder = new ScoreBuildingListener(importBase);
			ParseTreeWalker.DEFAULT.walk(builder, tree);

			SIDScoreIR.ScoreIR scoreIR = builder.buildScoreIR();
			SIDScoreIR.Resolver.Result result = new SIDScoreIR.Resolver().resolve(scoreIR);
			if (result.diagnostics().hasErrors()) {
				StringBuilder sb = new StringBuilder("Resolve errors:\n");
				for (var m : result.diagnostics().messages()) {
					if (m.severity() == SIDScoreIR.Diagnostics.Severity.ERROR) {
						sb.append("- ").append(m.text()).append('\n');
					}
				}
				setMessage(sb.toString(), MSG_ERROR);
				return null;
			}
			List<SIDScoreIR.Diagnostics.Message> warnings = result.diagnostics().messages().stream()
					.filter(m -> m.severity() == SIDScoreIR.Diagnostics.Severity.WARNING)
					.toList();
			if (showWarnings && !warnings.isEmpty()) {
				StringBuilder sb = new StringBuilder("Warnings:\n");
				for (var w : warnings) {
					sb.append("- ").append(w.text()).append('\n');
				}
				setMessage(sb.toString(), MSG_WARN);
			} else {
				clearMessage();
			}
			return result.timedScore();
		} catch (ScoreBuildingListener.ValidationException ex) {
			setMessage(ex.getMessage(), MSG_ERROR);
			if (showErrors) {
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
			}
			return null;
		} catch (IllegalStateException ex) {
			setMessage(ex.getMessage(), MSG_ERROR);
			if (showErrors) {
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
			}
			return null;
		} catch (RuntimeException ex) {
			setMessage(ex.getMessage(), MSG_ERROR);
			if (showErrors) {
				JOptionPane.showMessageDialog(frame, ex.getMessage(), "Parse Error", JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
	}

	private void clearMessage() {
		setMessage("", MSG_INFO);
	}

	private void setMessage(String message, Color color) {
		messageArea.setForeground(SCOPE_BG);
		messageArea.setText(message == null ? "" : message);
	}

	private void setMessageAsync(String message, Color color) {
		if (SwingUtilities.isEventDispatchThread()) {
			setMessage(message, color);
			return;
		}
		SwingUtilities.invokeLater(() -> setMessage(message, color));
	}

	private void appendMessageAsync(String message, Color color) {
		if (message == null || message.isBlank()) {
			return;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			appendMessage(message, color);
			return;
		}
		SwingUtilities.invokeLater(() -> appendMessage(message, color));
	}

	private void appendMessage(String message, Color color) {
		messageArea.setForeground(SCOPE_BG);
		String current = messageArea.getText();
		StringBuilder sb = new StringBuilder();
		if (current != null && !current.isBlank()) {
			sb.append(current);
			if (!current.endsWith("\n")) {
				sb.append('\n');
			}
		}
		sb.append(message);
		String out = sb.toString();
		if (compactViceLogs && out.length() > COMPACT_MESSAGES_MAX) {
			out = "...(truncated)\n" + out.substring(out.length() - COMPACT_MESSAGES_MAX);
		}
		messageArea.setText(out);
		messageArea.setCaretPosition(messageArea.getDocument().getLength());
	}

	private void copyErrorMessages() {
		String text = messageArea.getText();
		if (text == null || text.isBlank()) {
			return;
		}
		java.awt.Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.setContents(new java.awt.datatransfer.StringSelection(text), null);
		setMessage("Copied messages to clipboard.", MSG_INFO);
	}

	private void prepareHighlighting(SIDScoreIR.TimedScore timed, HighlightTimeline timeline) {
		playbackTicksPerSecond = timeline == HighlightTimeline.FRAMES
				? (timed.system() == SIDScoreIR.VideoSystem.NTSC ? RASTER_RATE_NTSC : RASTER_RATE_PAL)
				: timed.ticksPerWhole() / 4.0 * timed.tempoBpm() / 60.0;
		for (int i = 0; i < highlightIndices.length; i++) {
			highlightIndices[i] = 0;
		}
		if (lastParseTree == null) {
			highlightEventsByVoice = null;
			return;
		}
		highlightEventsByVoice = buildHighlightEvents(lastParseTree, timed, timeline);
		updatePlaybackHighlight();
	}

	private void updatePlaybackHighlight() {
		clearPlaybackHighlights();
		if (playbackStartNanos <= 0 || !isPlaying() || highlightEventsByVoice == null) {
			return;
		}
		double elapsedSeconds = (System.nanoTime() - playbackStartNanos) / 1_000_000_000.0;
		double elapsedTicks = elapsedSeconds * playbackTicksPerSecond;
		for (int voice = 1; voice <= 3; voice++) {
			List<EventSpan> spans = highlightEventsByVoice[voice];
			if (spans == null || spans.isEmpty()) {
				continue;
			}
			int idx = highlightIndices[voice];
			while (idx < spans.size() && elapsedTicks >= spans.get(idx).endTick) {
				idx++;
			}
			if (idx >= spans.size()) {
				highlightIndices[voice] = spans.size();
				continue;
			}
			highlightIndices[voice] = idx;
			EventSpan span = spans.get(idx);
			try {
				Object tag = input.getHighlighter().addHighlight(span.startOffset, span.endOffset, playbackPainter);
				playbackHighlightTags.add(tag);
			} catch (BadLocationException ignored) {
				// ignore invalid highlights
			}
		}
	}

	private void clearPlaybackHighlights() {
		Highlighter h = input.getHighlighter();
		for (Object tag : playbackHighlightTags) {
			h.removeHighlight(tag);
		}
		playbackHighlightTags.clear();
	}

	private void resetPlaybackHighlighting() {
		clearPlaybackHighlights();
		for (int i = 0; i < highlightIndices.length; i++) {
			highlightIndices[i] = 0;
		}
		highlightEventsByVoice = null;
		playbackTicksPerSecond = 0.0;
	}

	private List<EventSpan>[] buildHighlightEvents(SIDScoreParser.FileContext tree, SIDScoreIR.TimedScore timed,
			HighlightTimeline timeline) {
		@SuppressWarnings("unchecked")
		List<EventSpan>[] out = (List<EventSpan>[]) new List[4];
		for (SIDScoreParser.StmtContext stmt : tree.stmt()) {
			if (stmt.voiceBlock() == null) {
				continue;
			}
			SIDScoreParser.VoiceBlockContext voiceCtx = stmt.voiceBlock();
			int voiceIndex = Integer.parseInt(voiceCtx.INT().getText());
			if (voiceIndex < 1 || voiceIndex > 3) {
				continue;
			}
			List<Span> spans = collectEventSpans(voiceCtx.voiceItem());
			SIDScoreIR.TimedVoice tv = timed.voices().get(voiceIndex);
			if (tv == null) {
				continue;
			}
			List<SIDScoreIR.TimedEvent> events = tv.events();
			if (events.isEmpty() || spans.isEmpty()) {
				continue;
			}
			int count = Math.min(events.size(), spans.size());
			List<Integer> frameDurations = timeline == HighlightTimeline.FRAMES
					? computeFrameDurations(tv, timed)
					: List.of();
			List<EventSpan> voiceSpans = new java.util.ArrayList<>(count);
			int tickCursor = 0;
			for (int i = 0; i < count; i++) {
				SIDScoreIR.TimedEvent ev = events.get(i);
				Span sp = spans.get(i);
				int duration = ev.durationTicks();
				if (timeline == HighlightTimeline.FRAMES && i < frameDurations.size()) {
					duration = frameDurations.get(i);
				}
				if (duration < 1) {
					duration = 1;
				}
				int startTick = tickCursor;
				int endTick = tickCursor + duration;
				tickCursor = endTick;
				voiceSpans.add(new EventSpan(sp.startOffset, sp.endOffset, startTick, endTick));
			}
			out[voiceIndex] = voiceSpans;
		}
		return out;
	}

	private static List<Integer> computeFrameDurations(SIDScoreIR.TimedVoice voice, SIDScoreIR.TimedScore score) {
		if (voice == null || voice.instrument() == null || voice.events().isEmpty()) {
			return List.of();
		}
		List<Integer> out = new java.util.ArrayList<>(voice.events().size());
		double ticksPerQuarter = score.ticksPerWhole() / 4.0;
		double secondsPerTick = 60.0 / score.tempoBpm() / ticksPerQuarter;
		double frameRate = score.system() == SIDScoreIR.VideoSystem.PAL ? RASTER_RATE_PAL : RASTER_RATE_NTSC;
		double rem = 0.0;
		int gateMin = Math.max(0, voice.instrument().gateMin());

		for (SIDScoreIR.TimedEvent ev : voice.events()) {
			double framesExact = ev.durationTicks() * secondsPerTick * frameRate + rem;
			int frames = (int) Math.max(1, Math.round(framesExact));
			rem = framesExact - frames;
			if ((ev.type() == SIDScoreIR.TimedType.NOTE || ev.type() == SIDScoreIR.TimedType.NOISE) && gateMin > 0) {
				frames = Math.max(frames, gateMin);
			}
			out.add(frames);
		}
		return out;
	}

	private List<Span> collectEventSpans(List<SIDScoreParser.VoiceItemContext> items) {
		TieState tie = new TieState();
		List<Span> out = new java.util.ArrayList<>();
		appendEventSpans(items, out, tie, false);
		return out;
	}

	private void appendEventSpans(List<SIDScoreParser.VoiceItemContext> items, List<Span> out, TieState tie,
			boolean allowTieCarry) {
		for (SIDScoreParser.VoiceItemContext item : items) {
			if (item.AMP() != null) {
				tie.skipNextNote = true;
				continue;
			}
			if (item.noteOrRestOrHit() != null) {
				SIDScoreParser.NoteOrRestOrHitContext n = item.noteOrRestOrHit();
				org.antlr.v4.runtime.Token tok = null;
				boolean isNote = false;
				if (n.NOTE() != null) {
					tok = n.NOTE().getSymbol();
					isNote = true;
				} else if (n.REST() != null) {
					tok = n.REST().getSymbol();
				} else if (n.HIT() != null) {
					tok = n.HIT().getSymbol();
				}
				if (tok != null) {
					if (isNote && tie.skipNextNote) {
						tie.skipNextNote = false;
					} else {
						out.add(new Span(tok.getStartIndex(), tok.getStopIndex() + 1));
					}
				}
				continue;
			}
			if (item.legatoScope() != null) {
				appendEventSpans(item.legatoScope().voiceItem(), out, tie, true);
				continue;
			}
			if (item.tuplet() != null) {
				appendEventSpans(item.tuplet().voiceItem(), out, tie, true);
				continue;
			}
			if (item.repeat() != null) {
				int times = parseRepeatTimes(item.repeat().REPEAT_END().getText());
				for (int i = 0; i < times; i++) {
					TieState innerTie = new TieState();
					appendEventSpans(item.repeat().voiceItem(), out, innerTie, true);
				}
				if (!allowTieCarry) {
					tie.skipNextNote = false;
				}
			}
		}
	}

	private static int parseRepeatTimes(String tokenText) {
		int idx = tokenText.lastIndexOf('x');
		if (idx >= 0 && idx + 1 < tokenText.length()) {
			try {
				return Integer.parseInt(tokenText.substring(idx + 1));
			} catch (NumberFormatException ignored) {
				return 1;
			}
		}
		return 1;
	}

	private static final class TieState {
		private boolean skipNextNote = false;
	}

	private static final class Span {
		private final int startOffset;
		private final int endOffset;

		private Span(int startOffset, int endOffset) {
			this.startOffset = Math.max(0, startOffset);
			this.endOffset = Math.max(this.startOffset, endOffset);
		}
	}

	private static final class EventSpan {
		private final int startOffset;
		private final int endOffset;
		private final int startTick;
		private final int endTick;

		private EventSpan(int startOffset, int endOffset, int startTick, int endTick) {
			this.startOffset = startOffset;
			this.endOffset = endOffset;
			this.startTick = startTick;
			this.endTick = endTick;
		}
	}

	private enum HighlightTimeline {
		TICKS,
		FRAMES
	}

	private enum PlaybackRenderer {
		SRAP("SRAP"),
		VICE("VICE");

		private final String label;

		PlaybackRenderer(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private enum ExportFormat {
		ASM("ASM", ".asm"),
		WAV("WAV", ".wav"),
		SID("SID", ".sid"),
		PRG("PRG", ".prg");

		private final String label;
		private final String extension;

		ExportFormat(String label, String extension) {
			this.label = label;
			this.extension = extension;
		}
	}

	private static final class ExampleEntry {
		private final Path path;
		private final String label;

		private ExampleEntry(Path path, String label) {
			this.path = path;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static final class Metadata {
		private final String title;
		private final String author;

		private Metadata(String title, String author) {
			this.title = title;
			this.author = author;
		}
	}

	private static final class ThrowingErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			throw new RuntimeException("Syntax error at " + line + ":" + charPositionInLine + " - " + msg, e);
		}
	}

	private static final class OscilloscopePanel extends JPanel {
		private static final long serialVersionUID = -8816054112506868466L;
		private final float[] buffer = new float[SCOPE_BUFFER];
		private final float[] snapshot = new float[SCOPE_BUFFER];
		private final String title;
		private int writePos = 0;
		private boolean filled = false;

		OscilloscopePanel(String title) {
			this.title = title;
			setPreferredSize(new Dimension(720, 140));
			setBackground(SCOPE_BG);
			setOpaque(true);
		}

		synchronized void append(float[] samples, int length) {
			for (int i = 0; i < length; i++) {
				buffer[writePos] = samples[i];
				writePos = (writePos + 1) % buffer.length;
				if (writePos == 0) {
					filled = true;
				}
			}
		}

		synchronized void clear() {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = 0.0f;
			}
			writePos = 0;
			filled = false;
		}

		private synchronized int copySnapshot() {
			int len = filled ? buffer.length : writePos;
			if (len == 0) {
				return 0;
			}
			int start = filled ? writePos : 0;
			for (int i = 0; i < len; i++) {
				snapshot[i] = buffer[(start + i) % buffer.length];
			}
			return len;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int len = copySnapshot();
			int w = getWidth();
			int h = getHeight();
			if (w <= 1 || h <= 1) {
				return;
			}
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(SCOPE_BG);
			g2.fillRect(0, 0, w, h);

			int mid = h / 2;

			if (len == 0) {
				g2.setColor(SCOPE_TRACE);
				g2.setFont(C64_FONT_BOLD);
				g2.drawString(title, 8, 16);
				return;
			}

			g2.setColor(SCOPE_TRACE);
			float gain = 3.0f;
			int prevX = 0;
			int prevY = mid;
			for (int x = 0; x < w; x++) {
				int idx = (int) ((x / (float) (w - 1)) * (len - 1));
				float v = snapshot[idx] * gain;
				if (v > 1.0f) v = 1.0f;
				if (v < -1.0f) v = -1.0f;
				int y = mid - Math.round(v * (mid - 6));
				if (x > 0) {
					g2.drawLine(prevX, prevY, x, y);
				}
				prevX = x;
				prevY = y;
			}
			g2.setColor(SCOPE_TRACE);
			g2.setFont(C64_FONT_BOLD);
			g2.drawString(title, 8, 16);
		}
	}

	private static final class BannerPanel extends JPanel {
		private static final long serialVersionUID = -2837139728763804691L;
		private final BufferedImage image;

		BannerPanel(Path imagePath) {
			setPreferredSize(new Dimension(0, BANNER_HEIGHT));
			setBackground(C64_BG);
			BufferedImage loaded = null;
			try {
				if (Files.isRegularFile(imagePath)) {
					loaded = ImageIO.read(imagePath.toFile());
				}
			} catch (IOException ignored) {
				loaded = null;
			}
			image = loaded;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image == null) {
				g.setColor(C64_TEXT);
				g.setFont(C64_FONT_BOLD);
				g.drawString("SIDScore", 16, getHeight() / 2);
				return;
			}
			int w = getWidth();
			int h = getHeight();
			double scale = Math.min(w / (double) image.getWidth(), h / (double) image.getHeight());
			int drawW = (int) Math.round(image.getWidth() * scale);
			int drawH = (int) Math.round(image.getHeight() * scale);
			int x = 12;
			int y = (h - drawH) / 2;
			if (x + drawW > w) {
				x = Math.max(0, w - drawW);
			}
			g.drawImage(image, x, y, drawW, drawH, null);
		}
	}

	private static final class GrillePanel extends JComponent {
		private static final long serialVersionUID = 7866287330893052700L;

		GrillePanel() {
			int height = GRILLE_LINES + (GRILLE_LINES - 1) * GRILLE_SPACING + 2;
			setPreferredSize(new Dimension(0, height));
			setBackground(C64_BG);
			setOpaque(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			g.setColor(C64_BG);
			g.fillRect(0, 0, w, h);
			int margin = 8;
			int availableWidth = Math.max(0, w - (margin * 2));
			int y = 1;
			for (int i = 0; i < GRILLE_LINES; i++) {
				g.setColor((i % 2 == 0) ? C64_GRILLE_LIGHT : C64_GRILLE_DARK);
				g.drawLine(margin, y, margin + availableWidth, y);
				y += 1 + GRILLE_SPACING;
			}
		}
	}
}
