import $ from "jquery";

const Events = Object.freeze({
    CANDIDAT_CLIC_BTN_CONNEXION: 'candidat_clic_btn_connexion',
    CANDIDAT_RECHERCHE_OFFRE: 'candidat_recherche_offre',
    CANDIDAT_AFFICHAGE_DETAIL_OFFRE: 'candidat_affichage_detail_offre',
    CANDIDAT_FERMETURE_DETAIL_OFFRE: 'candidat_fermeture_detail_offre',
    CANDIDAT_AFFICHAGE_RESULTATS_RECHERCHE_OFFRE: 'candidat_affichage_resultats_recherche_offre',
    CANDIDAT_MODIFICATION_CRITERES_RECHERCHE_CONTACT: 'candidat_modification_criteres_recherche_contact',
    CANDIDAT_MODIFICATION_CRITERES_RECHERCHE_EMPLOI: 'candidat_modification_criteres_recherche_emploi',
    CANDIDAT_MODIFICATION_CRITERES_RECHERCHE_METIERS: 'candidat_modification_criteres_recherche_metiers',
    CANDIDAT_MODIFICATION_DISPONIBILITE: 'candidat_modification_disponibilite',
    CANDIDAT_CLIC_BTN_CONTACT_RECRUTEUR: 'candidat_clic_btn_contact_recruteur',
    CANDIDAT_CLIC_BTN_VOIR_OFFRE_SUR_POLE_EMPLOI: 'candidat_clic_btn_voir_offre_sur_pole_emploi',
    CANDIDAT_FERMETURE_MODALE_OFFRES_FRAUDULEUSES: 'candidat_fermeture_modale_offres_frauduleuses',
    RECRUTEUR_CLIC_BTN_CONNEXION: 'recruteur_clic_btn_connexion',
    RECRUTEUR_CLIC_BTN_VOIR_VIDEO: 'recruteur_clic_btn_voir_video',
    RECRUTEUR_CLIC_BTN_LIRE_ARTICLE: 'recruteur_clic_btn_lire_article',
    RECRUTEUR_CLIC_BTN_CONTACT_CANDIDAT: 'recruteur_clic_btn_contact_candidat',
    RECRUTEUR_PROFIL_MODIFIE: 'recruteur_profil_modifie',
    RECRUTEUR_RECHERCHE_CANDIDAT: 'recruteur_recherche_candidat',
    RECRUTEUR_AFFICHAGE_RESULTATS_RECHERCHE_CANDIDAT: 'recruteur_affichage_resultats_recherche_candidat',
    RECRUTEUR_AFFICHAGE_DETAIL_CANDIDAT: 'recruteur_affichage_detail_candidat',
    RECRUTEUR_FERMETURE_DETAIL_CANDIDAT: 'recruteur_fermeture_detail_candidat',
    RECRUTEUR_DETAIL_CANDIDAT_MRS: 'recruteur_detail_candidat_mrs',
    RECRUTEUR_DETAIL_CANDIDAT_POTENTIEL: 'recruteur_detail_candidat_potentiel',
    RECRUTEUR_DETAIL_CANDIDAT_PROFIL: 'recruteur_detail_candidat_profil',
    RECRUTEUR_DETAIL_CANDIDAT_EXPERIENCE: 'recruteur_detail_candidat_experience',
    RECRUTEUR_DETAIL_CANDIDAT_PAGINATION_SAVOIR_FAIRE: 'recruteur_detail_candidat_pagination_savoir_faire',
    AFFICHAGE_CGU: 'affichage_cgu',
    AFFICHAGE_CREDITS_PHOTOS: 'affichage_credits_photos',
    CONTACT_PAR_EMAIL: 'contact_par_email'
});

function sendEvent(event, context) {
    window.dataLayer = window.dataLayer || [];
    window.dataLayer.push(Object.assign({
        'event': event
    }, context));
}

function trackCommonActions() {
    trackCandidatLinks();
    trackRecruteurLinks();
    trackFooterLinks();
}

function trackCandidatLinks() {
    trackClickEvent('.js-menu-connexion-candidat', Events.CANDIDAT_CLIC_BTN_CONNEXION, {
        'source': 'menu'
    });
    trackClickEvent('.js-landing-candidat-btn-connexion', Events.CANDIDAT_CLIC_BTN_CONNEXION, {
        'source': 'landing_page_btn'
    });
    trackClickEvent('.js-candidat-btn-connexion-bandeau-detail-offre', Events.CANDIDAT_CLIC_BTN_CONNEXION, {
        'source': 'bandeau_detail_offre'
    });
    trackClickEvent('.js-candidat-btn-connexion-bandeau-liste-offres', Events.CANDIDAT_CLIC_BTN_CONNEXION, {
        'source': 'bandeau_liste_offres'
    });
}

function trackRecruteurLinks() {
    trackClickEvent('.js-menu-connexion-recruteur', Events.RECRUTEUR_CLIC_BTN_CONNEXION, {
        'source': 'menu'
    });
    trackClickEvent('.js-landing-recruteur-btn-connexion-1', Events.RECRUTEUR_CLIC_BTN_CONNEXION, {
        'source': 'landing_page_btn1'
    });
    trackClickEvent('.js-landing-recruteur-btn-connexion-2', Events.RECRUTEUR_CLIC_BTN_CONNEXION, {
        'source': 'landing_page_btn2'
    });
    trackClickEvent('.js-landing-recruteur-btn-connexion-3', Events.RECRUTEUR_CLIC_BTN_CONNEXION, {
        'source': 'landing_page_btn3'
    });
    trackClickEvent('.js-landing-recruteur-btn-voir-video', Events.RECRUTEUR_CLIC_BTN_VOIR_VIDEO);
    trackClickEvent('.js-landing-recruteur-btn-lire-article', Events.RECRUTEUR_CLIC_BTN_LIRE_ARTICLE);
    trackClickEvent('.js-recruteur-contacter-candidat', Events.RECRUTEUR_CLIC_BTN_CONTACT_CANDIDAT);
}

function trackFooterLinks() {
    trackClickEvent('.js-footer-cgu', Events.AFFICHAGE_CGU);
    trackClickEvent('.js-footer-credits-photos', Events.AFFICHAGE_CREDITS_PHOTOS);
    trackClickEvent('.js-footer-contact', Events.CONTACT_PAR_EMAIL);
}

function trackClickEvent(selector, event, context) {
    $(document).on('click', selector, () =>
        sendEvent(event, context ? context : {})
    );
}

export default {sendEvent, trackCommonActions, Events};
