import axios from "axios";

function afficherMessageSiPasDeMRS() {
    axios
        .get('/candidat/mrs')
        .then(function (response) {
            if (response.data.nbMRSValidees === 0) {
                window.location.assign('/candidat/pas-de-mrs');
            }
        });
}

export default afficherMessageSiPasDeMRS;
