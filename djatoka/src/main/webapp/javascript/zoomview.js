/*
 * A little script to add zoom functionality to the simple built in image
 * viewer in freelib-djatoka.
 */

function zoom(aImage) {
	document.getElementById('imageContainer').innerHTML = '<div id="zoom"/>';
	document.getElementById('caption').innerHTML = '';
	
	OpenSeadragon.DEFAULT_SETTINGS.autoHideControls = false;
	
	var closeButton = document.getElementById('bottomNavClose');
	var ts = new OpenSeadragon.DjTileSource("/view/", aImage);
	var viewer = new OpenSeadragon.Viewer("zoom");

	closeButton.setAttribute('onclick', 'window.location.reload()');
	viewer.openTileSource(ts);
}