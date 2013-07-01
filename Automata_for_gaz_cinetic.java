import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Automata_for_gaz_cinetic extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8126571549233828321L;
	public Affichage affichage;
	public AffichageControls affcontrol;
	public DessinControl dcontrol;
	public Ustensile ustens;
	public JFrame f;
	public Bougesouris bsouris;
	public Clicsouris csouris;
	public Rectangle recta;

	public void init(JFrame f) {
		this.f = f;
		affichage = new Affichage();
		affcontrol = new AffichageControls(affichage, this);
		dcontrol = new DessinControl(affichage, this);
		ustens = new Ustensile();
		bsouris = new Bougesouris(affichage, dcontrol, ustens);
		csouris = new Clicsouris(affichage, dcontrol, ustens);
		recta = new Rectangle();
		getContentPane().add("Center", affichage);
		getContentPane().add("South", affcontrol);
		getContentPane().add("West", ustens);
		getContentPane().add("North", dcontrol);
		affichage.addMouseMotionListener(bsouris);
		affichage.addMouseListener(csouris);
	}

	public void start() {
		affichage.start();
	}

	public void stop() {
		affichage.stop();
	}

	class Affichage extends JPanel implements Runnable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5133905934738505321L;
		int w = 10;
		int h = 10;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D gi = bi.createGraphics();
		Font f = new Font("TimesRoman", Font.BOLD, 12);
		int[] tableverite;
		int[][][] matrice;
		float[][][] presentation;
		float sin60, demi;
		int taillemaxx, taillemaxy, taclust;
		int drapvif, drapmor, inter;
		int x, y, xcluster, ycluster, ix, iy;
		float echelle;
		int test, xx, pif;
		private Thread thread;
		private BufferedImage bimg;
		Graphics g;
		int ii, i;

		public Affichage() {
			g = getGraphics();
			setBackground(Color.white);
			taillemaxx = 1000;
			taillemaxy = 1000;
			taclust = 16;
			sin60 = (float) (Math.sin(Math.PI / 3));
			demi = (float) (0.5);
			tableverite = new int[256];
			drapvif = 1;
			matrice = new int[2][taillemaxx][taillemaxy];
			System.out.println("remplissage");
			presentation = new float[taillemaxx][taillemaxy][2];

			// remplissage des tables par defaut
			for (i = 0; i < 128; i++) {
				tableverite[i] = i;
				for (ii = 0; ii < 8; ii++) {
				}
			}

			// collisions entre 2 particules a 120 degres forment 1 immobile
			// plus 1 tout droit (et inversememt)
			int mo2, pl2;
			for (i = 0; i < 6; i++) {
				ii = (int) (Math.pow(2, i));
				mo2 = ((ii >> 1) | (ii << (6 - 1))) & 0x0000003f;
				pl2 = ((ii << 1) | (ii >>> (6 - 1))) & 0x0000003f;
				tableverite[ii | 64] = (mo2 | pl2);
				tableverite[(mo2 | pl2)] = (ii | 64);

			}

			// collision entre 3 particules
			tableverite[42] = 21;
			tableverite[21] = 42;

			/*
			 * collision entre 2 particules face a face ( presence d'un flag
			 * (128) pour indiquer un choix aleatoire a deux possibilites)
			 */

			tableverite[9] = 128;
			tableverite[128 + 9] = 18;
			tableverite[18] = 128;
			tableverite[128 + 18] = 36;
			tableverite[36] = 128;
			tableverite[128 + 36] = 9;

		}

		public void pointparpoint(Graphics2D g) {

			for (x = 0; x < (taillemaxx); x++) {
				for (y = 0; y < (taillemaxy); y++) {
					test = (matrice[drapmor][x][y] & (dcontrol.affich.valeur << ustens.norperm)) >>> ustens.norperm;
					if (test != 0) {
						for (ii = 0; ii < 7; ii++) {
							if (((test >>> ii) & 1) != 0) {
								g.setColor(dcontrol.couleur.tabcoul[ii + 1]);
								g.drawLine(x, y, x, y);
								ii = 8;
							}
						}
						if (((test >>> 8) & 1) != 0) {
							g.setColor(dcontrol.couleur.tabcoul[8]);
							g.drawLine(x, y, x, y);
						}
					}
				}
			}
		}

		public void prpoint(int x, int y) {
			if (ustens.stylgom == true) {
				matrice[drapmor][x][y] = matrice[drapmor][x][y] | (dcontrol.crayon.valeur << ustens.norperm);
			} else {
				matrice[drapmor][x][y] -= matrice[drapmor][x][y] & (dcontrol.crayon.valeur << ustens.norperm);
			}
		}

		public void clusterise(Graphics2D g) {

			echelle = taclust / (float) (taclust * taclust);
			echelle = echelle * 2;
			g.setFont(f);
			for (x = 0; x < (taillemaxx); x++) {
				for (y = 0; y < (taillemaxy); y++) {
					presentation[x][y][0] = 0;
					presentation[x][y][1] = 0;
				}
			}
			xcluster = 0;
			ycluster = 0;
			for (x = 0; x < (taillemaxx); x++) {
				for (y = 0; y < (taillemaxy); y++) {
					xcluster = x / taclust;
					ycluster = y / taclust;
					presentation[xcluster][ycluster][1] += (+((float) ((matrice[drapmor][x][y] & 8) >>> 3) * sin60) + ((float) ((matrice[drapmor][x][y] & 16) >>> 4) * sin60)
							- ((float) ((matrice[drapmor][x][y] & 1) >>> 0) * sin60) - ((float) ((matrice[drapmor][x][y] & 2) >>> 1) * sin60));
					presentation[xcluster][ycluster][0] += (-((float) ((matrice[drapmor][x][y] & 16) >>> 4) * demi) - ((float) ((matrice[drapmor][x][y] & 32) >>> 5))
							- ((float) ((matrice[drapmor][x][y] & 1) >>> 0) * demi) + ((float) ((matrice[drapmor][x][y] & 2) >>> 1) * demi) + ((float) ((matrice[drapmor][x][y] & 4) >>> 2)) + ((float) ((matrice[drapmor][x][y] & 8) >>> 3) * demi));

				}
			}
			for (x = 0; x < (taillemaxx / taclust); x++) {
				for (y = 0; y < (taillemaxy / taclust); y++) {
					if ((presentation[x][y][0] != 0) || (presentation[x][y][1] != 0)) {
						g.setColor(Color.black);

						g.drawString(".", x * (taclust) + (int) (Math.round(presentation[x][y][0] * echelle)), y * (taclust) + (int) (Math.round(presentation[x][y][1] * echelle)));
						g.setColor(Color.red);
						g.drawLine(x * (taclust), y * (taclust), (int) (Math.round(presentation[x][y][0] * echelle) + x * (taclust)), (int) (Math.round(presentation[x][y][1] * echelle) + y
								* (taclust)));
						presentation[x][y][0] = 0;
						presentation[x][y][1] = 0;
					}
				}
			}
		}

		public void calcul() {

			test = 0;
			for (x = 2; x < (taillemaxx - 2); x++) {
				for (y = 2; y < (taillemaxy - 2); y++) {
					if ((matrice[drapmor][x][y] != 0) & (matrice[drapmor][x][y] != 64)) {
						if (((matrice[drapmor][x][y]) & 256) != 0) {
							ii = matrice[drapmor][x][y] & 63;
							test = ((ii >> 3) | (ii << (6 - 3))) & 63;
							test = test | (matrice[drapmor][x][y] & 64);
						} else {
							test = tableverite[(matrice[drapmor][x][y] & 255)];
						}
						xx = y % 2 + x - 1;
						if (test == 128) {
							test = tableverite[(matrice[drapmor][x][y] & 255) + 128];
							pif = (int) (Math.round(Math.random()) + 1) * (test & 0x0000003f);
							if (pif > 36) {
								pif = 9;
							}
							test = (test & 64) + (pif);
						}
						test = test | ((matrice[drapmor][x][y] >>> 9) & 127);
						test = test - (test & ((matrice[drapmor][x][y] >>> 16) & 127));
						matrice[drapvif][x][y] += (matrice[drapmor][x][y] & 0xfffff00) | (test & 64);

						matrice[drapvif][xx][y - 1] += (test & 1);
						matrice[drapvif][xx + 1][y - 1] += (test & 2);
						matrice[drapvif][x + 1][y] += (test & 4);
						matrice[drapvif][xx + 1][y + 1] += (test & 8);
						matrice[drapvif][xx][y + 1] += (test & 16);
						matrice[drapvif][x - 1][y] += (test & 32);
						matrice[drapmor][x][y] = 0;
					} else {
						matrice[drapvif][x][y] += matrice[drapmor][x][y];
					}
				}
			}
			inter = drapvif;
			drapvif = drapmor;
			drapmor = inter;
		}

		public Graphics2D createGraphics2D(int w, int h) {
			Graphics2D g2 = null;
			if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
				bimg = (BufferedImage) createImage(w, h);

			}
			g2 = bimg.createGraphics();
			g2.setBackground(getBackground());
			g2.clearRect(0, 0, w, h);
			return g2;
		}

		public void paint(Graphics h) {
			g = h;
			if (affcontrol.souriscalcul == true) {
				calcul();
				System.out.print(".");
			}
			if (affcontrol.sourismod != "2") {
				Dimension d = getSize();
				Graphics2D g2 = createGraphics2D(d.width, d.height);
				if (affcontrol.sourismod == "1") {
					clusterise(g2);
				} else {
					pointparpoint(g2);
				}
				g2.dispose();
				g.drawImage(bimg, 0, 0, this);
			}

		}

		public void start() {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}

		public synchronized void stop() {
			thread = null;
		}

		public void run() {
			Thread me = Thread.currentThread();
			while (thread == me) {
				repaint();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
			thread = null;
		}
	}

	class AffichageControls extends JPanel implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4879025809171858026L;
		Affichage affichage;
		Automata_for_gaz_cinetic automate;
		String zero = "0";
		String un = "1";
		String deux = "2";
		String pause = "pause";
		String sourismod;
		boolean souriscalcul;
		JRadioButton point, flux, pasdimage;
		JButton butpause;
		BorderLayout bords;
		JToolBar affich;
		Font font = new Font("serif", Font.PLAIN, 10);

		public AffichageControls(Affichage affichage, Automata_for_gaz_cinetic automate) {
			this.affichage = affichage;
			this.automate = automate;
			setBackground(Color.black);
			bords = new BorderLayout();
			setLayout(bords);
			souriscalcul = false;
			sourismod = "0";
			butpause = new JButton("DEMARRER");
			butpause.setActionCommand(pause);
			butpause.addActionListener(this);
			butpause.setPreferredSize(new Dimension(160, 45));

			point = new JRadioButton("Afficher les points");
			point.setActionCommand(zero);
			point.setSelected(true);
			point.addActionListener(this);
			point.setPreferredSize(new Dimension(160, 45));

			flux = new JRadioButton("Afficher les flux");
			flux.setActionCommand(un);
			flux.addActionListener(this);
			flux.setPreferredSize(new Dimension(160, 45));

			pasdimage = new JRadioButton("Ne rien afficher");
			pasdimage.setActionCommand(deux);
			pasdimage.addActionListener(this);
			pasdimage.setPreferredSize(new Dimension(160, 45));

			ButtonGroup group = new ButtonGroup();
			group.add(point);
			group.add(flux);
			group.add(pasdimage);
			affich = new JToolBar(JToolBar.HORIZONTAL);
			affich.setFloatable(false);
			affich.setBackground(Color.black);
			affich.addSeparator();
			affich.add(butpause);
			affich.addSeparator();
			affich.add(point);
			affich.addSeparator();
			affich.add(flux);
			affich.addSeparator();
			affich.add(pasdimage);
			affich.addSeparator();
			add(affich);

		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() != pause) {
				sourismod = e.getActionCommand();
			} else {
				if (butpause.getText() == "PAUSE") {
					souriscalcul = false;
					butpause.setText("DEMARRER");
				} else {
					souriscalcul = true;
					butpause.setText("PAUSE");
				}
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(160, 50);
		}
	} // End AffichageControls class

	class DessinControl extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3626362135184585500L;
		Affichage affichage;
		Automata_for_gaz_cinetic automate;
		Hexagone crayon, affich, couleur;
		int valeur;
		Font font = new Font("serif", Font.PLAIN, 10);

		public DessinControl(Affichage affichage, Automata_for_gaz_cinetic automate) {
			this.affichage = affichage;
			this.automate = automate;
			setBackground(Color.black);
			setLayout(new FlowLayout());
			crayon = new Hexagone("crayon", this);
			add(crayon);
			TitledBorder tb = new TitledBorder(new EtchedBorder());
			tb.setTitleFont(font);
			tb.setTitle("CRAYONS");
			tb.setTitleColor(Color.white);
			crayon.setBorder(tb);
			affich = new Hexagone("affich", this);
			add(affich);
			tb = new TitledBorder(new EtchedBorder());
			tb.setTitleFont(font);
			tb.setTitle("CALQUES");
			tb.setTitleColor(Color.white);
			affich.setBorder(tb);
			affich.changeval(127 + 512);
			couleur = new Hexagone("couleur", this);
			add(couleur);
			tb = new TitledBorder(new EtchedBorder());
			tb.setTitleFont(font);
			tb.setTitle("COULEURS");
			tb.setTitleColor(Color.white);
			couleur.setBorder(tb);
		}
	}

	class Hexagone extends JToolBar {

		/**
	 * 
	 */
		private static final long serialVersionUID = -3609758537674008856L;
		DessinControl per;
		JButton[] tabut = new JButton[9];
		ImageIcon[][] tabimage = new ImageIcon[9][2];
		Color[] tabcoul = { Color.blue, Color.yellow, Color.red, Color.green, Color.orange, Color.black, Color.gray, Color.pink, Color.blue };
		Insets bords;
		int valeur, ii;

		public Hexagone(String genre, DessinControl pere) {
			super();
			per = pere;
			ledessin();
			if (genre == "couleur") {
				initialiscouleur();
			} else {
				initialisecrayaff();
				if (genre == "crayon") {
					for (ii = 1; ii < 9; ii++) {
						tabut[ii].addActionListener(new Crayon(ii));
					}
				} else {
					for (ii = 1; ii < 9; ii++) {
						tabut[ii].addActionListener(new Afficha(ii));
					}
				}
			}
		}

		public void ledessin() {
			int ii;
			setFloatable(true);
			setBackground(Color.gray);
			setLayout(null);
			bords = getInsets();
			addSeparator();
			for (ii = 1; ii < 9; ii++) {
				tabut[ii] = new JButton();
				add(tabut[ii]);
				addSeparator();
			}

			tabut[1].setBounds(20 + bords.left, 15 + bords.top, 20, 20);
			tabut[2].setBounds(40 + bords.left, 15 + bords.top, 20, 20);
			tabut[6].setBounds(10 + bords.left, 35 + bords.top, 20, 20);
			tabut[7].setBounds(30 + bords.left, 35 + bords.top, 20, 20);
			tabut[3].setBounds(50 + bords.left, 35 + bords.top, 20, 20);
			tabut[5].setBounds(20 + bords.left, 55 + bords.top, 20, 20);
			tabut[4].setBounds(40 + bords.left, 55 + bords.top, 20, 20);
			tabut[8].setBounds(75 + bords.left, 35 + bords.top, 20, 40);
		}

		public Dimension getPreferredSize() {
			return new Dimension(135, 90);
		}

		public void initialiscouleur() {
			for (ii = 1; ii < 9; ii++) {
				tabut[ii].setBackground(tabcoul[ii]);
				tabut[ii].addActionListener(new Coule(ii));
			}
		}

		public void initialisecrayaff() {

			tabimage[1][0] = new ImageIcon("hg.gif");
			tabimage[2][0] = new ImageIcon("hd.gif");
			tabimage[6][0] = new ImageIcon("mg.gif");
			tabimage[7][0] = new ImageIcon("centre.gif");
			tabimage[3][0] = new ImageIcon("md.gif");
			tabimage[5][0] = new ImageIcon("bg.gif");
			tabimage[4][0] = new ImageIcon("bd.gif");
			tabimage[8][0] = new ImageIcon("bordn.gif");

			tabimage[1][1] = new ImageIcon("hgj.gif");
			tabimage[2][1] = new ImageIcon("hdj.gif");
			tabimage[6][1] = new ImageIcon("mgj.gif");
			tabimage[7][1] = new ImageIcon("centrej.gif");
			tabimage[3][1] = new ImageIcon("mdj.gif");
			tabimage[5][1] = new ImageIcon("bgj.gif");
			tabimage[4][1] = new ImageIcon("bdj.gif");
			tabimage[8][1] = new ImageIcon("bordj.gif");
			for (ii = 1; ii < 9; ii++) {
				tabut[ii].setIcon(tabimage[ii][0]);
			}
		}

		public void changeval(int vale) {
			valeur = vale;
			for (ii = 1; ii < 8; ii++) {
				tabut[ii].setIcon(tabimage[ii][(valeur >>> (ii - 1)) & 1]);
			}
			tabut[8].setIcon(tabimage[8][(valeur >>> 8) & 1]);
		}

		class Coule implements ActionListener {
			int vale;

			public Coule(int valeu) {
				vale = valeu;
			}

			public void actionPerformed(ActionEvent e) {
				affichage.stop();
				Color newColor = JColorChooser.showDialog(tabut[vale], "Choisissez votre couleur", tabcoul[vale]);
				if (newColor != null) {
					tabcoul[vale] = newColor;
					tabut[vale].setBackground(tabcoul[vale]);
				}
				affichage.start();
			}
		}

		class Afficha implements ActionListener {
			int vale, pui;

			public Afficha(int valeu) {
				vale = valeu;
				pui = (int) (Math.pow(2, (vale - 1)));
				if (pui == 128) {
					pui = pui * 2;
				}
			}

			public void actionPerformed(ActionEvent e) {

				if (tabut[vale].getIcon() == tabimage[vale][0]) {
					valeur = valeur | pui;
					tabut[vale].setIcon(tabimage[vale][1]);
				} else {
					valeur = valeur - (valeur & (pui));
					tabut[vale].setIcon(tabimage[vale][0]);
				}
				per.crayon.changeval((per.crayon.valeur) & valeur);

			}
		}

		class Crayon implements ActionListener {
			int vale, pui;

			public Crayon(int valeu) {
				vale = valeu;
				pui = (int) (Math.pow(2, (vale - 1)));
				if (pui == 128) {
					pui = pui * 2;
				}
			}

			public void actionPerformed(ActionEvent e) {

				if (tabut[vale].getIcon() == tabimage[vale][0]) {
					valeur = valeur | pui;
					tabut[vale].setIcon(tabimage[vale][1]);
				} else {
					valeur = valeur - (valeur & (pui));
					tabut[vale].setIcon(tabimage[vale][0]);
				}
				per.affich.changeval((per.affich.valeur) | valeur);

			}
		}
	}

	class Ustensile extends JPanel implements ActionListener {

		/**
	 * 
	 */
		private static final long serialVersionUID = -7729713724422069476L;
		Affichage affichage;
		Automata_for_gaz_cinetic automate;
		JToolBar menu1, menu2, menu3;
		Font font = new Font("serif", Font.PLAIN, 10);
		JRadioButton stylo, gomme, normal, permanent, trou, point, carre, rectangle, tout;
		boolean stylgom;
		String nomfig;
		int norperm;
		ButtonGroup stylgomg, norpermg, nomfigg;

		public Ustensile() {

			setBackground(Color.black);
			setLayout(new FlowLayout());
			nomfig = "point";
			stylgom = true;
			norperm = 0;
			stylo = new JRadioButton("Stylo");
			stylo.setActionCommand("stylo");
			stylo.setSelected(true);
			stylo.addActionListener(this);
			stylo.setPreferredSize(new Dimension(80, 40));

			gomme = new JRadioButton("Gomme");
			gomme.setActionCommand("gomme");
			gomme.addActionListener(this);
			gomme.setPreferredSize(new Dimension(80, 40));

			stylgomg = new ButtonGroup();
			stylgomg.add(stylo);
			stylgomg.add(gomme);

			normal = new JRadioButton("Normal");
			normal.setActionCommand("normal");
			normal.setSelected(true);
			normal.addActionListener(this);
			normal.setPreferredSize(new Dimension(80, 40));

			permanent = new JRadioButton("Permanent");
			permanent.setActionCommand("permanent");
			permanent.addActionListener(this);
			permanent.setPreferredSize(new Dimension(80, 40));

			trou = new JRadioButton("Trou");
			trou.setActionCommand("trou");
			trou.addActionListener(this);
			trou.setPreferredSize(new Dimension(80, 40));

			norpermg = new ButtonGroup();
			norpermg.add(normal);
			norpermg.add(permanent);
			norpermg.add(trou);

			point = new JRadioButton("Ligne");
			point.setActionCommand("point");
			point.setSelected(true);
			point.addActionListener(this);
			point.setPreferredSize(new Dimension(80, 40));

			carre = new JRadioButton("Carre");
			carre.setActionCommand("carre");
			carre.addActionListener(this);
			carre.setPreferredSize(new Dimension(80, 40));

			rectangle = new JRadioButton("Rectangle");
			rectangle.setActionCommand("rectangle");
			rectangle.addActionListener(this);
			rectangle.setPreferredSize(new Dimension(80, 40));

			tout = new JRadioButton("Tout");
			tout.setActionCommand("tout");
			tout.addActionListener(this);
			tout.setPreferredSize(new Dimension(80, 40));
			nomfigg = new ButtonGroup();
			nomfigg.add(point);
			nomfigg.add(carre);
			nomfigg.add(rectangle);
			nomfigg.add(tout);

			menu1 = new JToolBar(JToolBar.VERTICAL);
			menu1.setFloatable(true);
			menu1.setBackground(Color.gray);
			menu1.addSeparator();
			menu1.add(stylo);
			menu1.addSeparator();
			menu1.add(gomme);
			menu1.addSeparator();
			add(menu1);

			menu2 = new JToolBar(JToolBar.VERTICAL);
			menu2.setFloatable(true);
			menu2.setBackground(Color.gray);
			menu2.addSeparator();
			menu2.add(normal);
			menu2.addSeparator();
			menu2.add(permanent);
			menu2.addSeparator();
			menu2.add(trou);
			menu2.addSeparator();
			add(menu2);

			menu3 = new JToolBar(JToolBar.VERTICAL);
			menu3.setFloatable(true);
			menu3.setBackground(Color.gray);
			menu3.addSeparator();
			menu3.add(point);
			menu3.addSeparator();
			menu3.add(carre);
			menu3.addSeparator();
			menu3.add(rectangle);
			menu3.addSeparator();
			menu3.add(tout);
			menu3.addSeparator();
			add(menu3);
			TitledBorder tb = new TitledBorder(new EtchedBorder());
			tb.setTitleFont(font);
			tb.setTitle(" ");
			menu1.setBorder(tb);
			tb = new TitledBorder(new EtchedBorder());
			tb.setTitleFont(font);
			tb.setTitle(" ");
			menu2.setBorder(tb);
			tb = new TitledBorder(new EtchedBorder());
			tb.setTitleFont(font);
			tb.setTitle(" ");
			menu3.setBorder(tb);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() == "stylo") {
				stylgom = true;
			}
			if (e.getActionCommand() == "gomme") {
				stylgom = false;
			}
			if (e.getActionCommand() == "normal") {
				norperm = 0;
			}
			if (e.getActionCommand() == "permanent") {
				norperm = 9;
			}
			if (e.getActionCommand() == "trou") {
				norperm = 16;
			}
			if (e.getActionCommand() == "point") {
				nomfig = "point";
			}
			if (e.getActionCommand() == "carre") {
				nomfig = "carre";
			}
			if (e.getActionCommand() == "rectangle") {
				nomfig = "rectangle";
			}
			if (e.getActionCommand() == "tout") {
				nomfig = "tout";
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(100, 0);
		}
	}

	class Bougesouris implements MouseMotionListener {

		Affichage affichage;
		DessinControl dcontrol;
		Ustensile u;
		int xp, xxp, yp, yyp, gintde, gintar, boucle1, boucle3, dey, ary;
		float pint, boucle2;

		public Bougesouris(Affichage aff, DessinControl dcon, Ustensile ustensi) {
			affichage = aff;
			dcontrol = dcon;
			u = ustensi;
		}

		public void mouseMoved(MouseEvent evt) {
			xxp = evt.getX();
			yyp = evt.getY();
			recta.x1 = xxp;
			recta.y1 = yyp;
		}

		public void mouseDragged(MouseEvent evt) {

			if ((evt.getX() < affichage.taillemaxx) && (evt.getY() < affichage.taillemaxy)) {
				recta.x2 = evt.getX();
				recta.y2 = evt.getY();
				if (u.nomfig == "point") {
					xp = evt.getX();
					yp = evt.getY();
					if ((Math.abs(xp - xxp)) >= (Math.abs(yp - yyp))) {
						if (xp <= xxp) {
							gintde = xp;
							gintar = xxp;
							boucle2 = yp;
						} else {
							gintde = xxp;
							gintar = xp;
							boucle2 = yyp;
						}
						pint = (((float) (yp - yyp)) / ((float) (xp - xxp)));

						for (boucle1 = gintde; boucle1 <= gintar; boucle1++) {
							affichage.prpoint(boucle1, (int) Math.round(boucle2));
							boucle2 += pint;
						}
					} else {
						if (yp <= yyp) {
							gintde = yp;
							gintar = yyp;
							boucle2 = xp;
						} else {
							gintde = yyp;
							gintar = yp;
							boucle2 = xxp;
						}
						pint = (((float) (xp - xxp)) / ((float) (yp - yyp)));

						for (boucle1 = gintde; boucle1 <= gintar; boucle1++) {
							affichage.prpoint((int) Math.round(boucle2), boucle1);
							boucle2 += pint;
						}
					}
					xxp = xp;
					yyp = yp;
				} else if (u.nomfig == "carre") {
					gintde = Math.max(0, evt.getX() - 8);
					gintar = Math.min(affichage.taillemaxx, evt.getX() + 8);
					dey = Math.max(0, evt.getY() - 8);
					ary = Math.min(affichage.taillemaxy, evt.getY() + 8);
					for (boucle1 = gintde; boucle1 <= gintar; boucle1++) {
						for (boucle3 = dey; boucle3 <= ary; boucle3++) {
							affichage.prpoint(boucle1, boucle3);
						}
					}
				}
			}
		}

	}

	class Clicsouris implements MouseListener {
		int x, y;
		Affichage affichage;
		DessinControl dcontrol;
		Ustensile u;

		public Clicsouris(Affichage aff, DessinControl dcon, Ustensile ustensi) {
			affichage = aff;
			dcontrol = dcon;
			u = ustensi;
		}

		public void mouseClicked(MouseEvent evt) {

		}

		public void mouseEntered(MouseEvent evt) {
		}

		public void mouseExited(MouseEvent evt) {
		}

		public void mousePressed(MouseEvent evt) {
			if (u.nomfig == "tout") {
				for (x = 0; x < affichage.taillemaxx; x++) {
					for (y = 0; y < affichage.taillemaxy; y++) {
						affichage.prpoint(x, y);
					}
				}
			} else {
				bsouris.mouseDragged(evt);
			}
		}

		public void mouseReleased(MouseEvent evt) {
			if (u.nomfig == "rectangle") {
				recta.faire();
			}
		}
	}

	class Rectangle {
		public int x1, y1, x2, y2;
		int x, y, xmi, xma, ymi, yma;

		public Rectangle() {
		}

		public void faire() {
			xmi = Math.min(x1, x2);
			xma = Math.max(x1, x2);
			ymi = Math.min(y1, y2);
			yma = Math.max(y1, y2);
			for (x = xmi; x <= xma; x++) {
				for (y = ymi; y <= yma; y++) {
					affichage.prpoint(x, y);
				}
			}
		}
	}

	public static void main(String argv[]) {
		final Automata_for_gaz_cinetic affichage = new Automata_for_gaz_cinetic();
		JFrame f = new JFrame("Automates");
		affichage.init(f);

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowDeiconified(WindowEvent e) {
				affichage.start();
			}

			public void windowIconified(WindowEvent e) {
			}
		});
		f.getContentPane().add("Center", affichage);

		f.setSize(new Dimension(800, 800));
		f.setVisible(true);

		affichage.start();
	}
}
