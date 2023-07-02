/*******************************************************************************************************
 *
 * CritereFonctionsCroyances.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.extensions.multi_criteria;

/**
 * @author PTaillandier
 * Crit�re destin� � la m�thode de d�cision multicrit�re bas�e sur les fonctions de croyance (voir th�se, Chap E.)
 */
public abstract class CritereFonctionsCroyances {

	/** The nom. */
	//nom du crit�re
	private String nom;

	/**
	 * Instantiates a new critere fonctions croyances.
	 *
	 * @param nom the nom
	 */
	protected CritereFonctionsCroyances(final String nom) {
		this.nom = nom;
	}

	/**
	 * Gets the nom.
	 *
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Sets the nom.
	 *
	 * @param nom the new nom
	 */
	public void setNom(final String nom) {
		this.nom = nom;
	}

	@Override
	public String toString() {
		return nom;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (nom == null ? 0 : nom.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if ( this == obj ) { return true; }
		if ( obj == null ) { return false; }
		if ( getClass() != obj.getClass() ) { return false; }
		final CritereFonctionsCroyances other = (CritereFonctionsCroyances) obj;
		if ( nom == null ) {
			if ( other.nom != null ) { return false; }
		} else if ( !nom.equals(other.nom) ) { return false; }
		return true;
	}

	/**
	 * M�thode d'initialisation de la masse de croyance pour ce crit�re de "ce candidat est le meilleur"
	 * @param a : valeur courante du crit�re
	 * @return la valeur de la masse de croyance
	 */
	public abstract double masseCroyancePour(double a);

	/**
	 * M�thode d'initialisation de la masse de croyance pour ce crit�re de "ce candidat n'est pas le meilleur"
	 * @param a : valeur courante du crit�re
	 * @return la valeur de la masse de croyance
	 */
	public abstract double masseCroyanceContre(double a);

	/**
	 * M�thode d'initialisation de la masse de croyance pour ce crit�re de "je ne sais pas si ce candidate est le meilleur"
	 * @param a : valeur courante du crit�re
	 * @return la valeur de la masse de croyance
	 */
	public abstract double masseCroyanceIgnorance(double a);

}
