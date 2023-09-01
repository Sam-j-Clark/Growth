$(document).ready(() => {
    $(".backButton").click(function () {
        location.href = "account"
    })

    $(".deleteProfilePhotoButton").on("click", () => {
        $.ajax({
            url: "deleteProfileImg",
            type: "DELETE",
            success: function () {
                sendNotification("", userIdent, "update user photo")
                location.reload()
            }
        })
    });
})


/**
 * Creates a new Image object and waits for it to load before continuing
 * @param imageUrl src url of new image object ot be created
 * @returns {Promise<*>}
 */
async function loadImage(imageUrl) {
    let img;
    const imageLoadPromise = new Promise(resolve => {
        img = new Image();
        img.onload = resolve;
        img.src = imageUrl;
    });

    await imageLoadPromise;
    return img

}


/**
 *  Resizes image to desired width and height. Adjusts image quality according to constant value.
 */
async function processImage() {

    // Get elements from HTML page
    const previewImage = document.getElementById('profileImagePreview');
    const fileUploadInput = document.getElementById('profileImageInput');
    const loadingText = document.getElementById('loadingText');

    loadingText.style.display = "inline"; // Make loading gif visible

    // Create canvas and context objects
    const canvas = document.createElement("canvas");
    const context = canvas.getContext("2d");

    // Set constants
    const MAX_FILE_SIZE = 5000000; // 5MB
    const MAX_CANVAS_DIMENSION = 8000; // Max canvas size for google chrome

    // Initialize Variables
    const uploadImageDataURL = URL.createObjectURL(fileUploadInput.files[0]);
    const uploadImageObject = await loadImage(uploadImageDataURL);
    const originalWidth = uploadImageObject.width;
    const originalHeight = uploadImageObject.height;
    let newHeight;
    let newWidth;
    let imageSize = fileUploadInput.files[0].size;
    let quality;

    // Calculate quality value needed
    if (imageSize <= MAX_FILE_SIZE) {
        quality = 0.9;
    } else {
        quality = MAX_FILE_SIZE / imageSize;
    }

    // Find smaller dimension of image for making square
    if (originalWidth <= originalHeight) {
        newHeight = originalWidth;
        newWidth = originalWidth;
    } else {
        newHeight = originalHeight;
        newWidth = originalHeight;
    }

    // Resizing the image
    const cropOffsetX = 0;
    const cropOffsetY = 0;

    canvas.width = Math.min(newWidth, MAX_CANVAS_DIMENSION);
    canvas.height = Math.min(newHeight, MAX_CANVAS_DIMENSION);

    // Image with new parameters is drawn using canvas object
    context.drawImage(uploadImageObject, cropOffsetX, cropOffsetY, newWidth, newHeight, 0, 0, newWidth, newHeight);
    // Compressing the image and converting to jpeg using a blob object
    canvas.toBlob(
        (blob) => {
            if (blob) {
                // showing the compressed image
                previewImage.src = URL.createObjectURL(blob);
                document.querySelector("#size").innerHTML = bytesToSize(blob.size); // Sends image size to upload form
            } else {
                alert("Error compressing image - try a smaller file");
            }
            loadingText.style.display = "none"; // Make loading gif invisible

        },
        "image/jpeg",
        quality
    );
}


async function sendImagePostRequest() {
    const url = document.getElementById('profileImagePreview').getAttribute('src');
    document.getElementById('profile').setAttribute('src', url);
    const formData = new FormData();
    formData.append("image", await fetch(url).then(r => r.blob()));

    await fetch("upload", {
        method: "POST",
        body: formData
    }).then((response) => response.json())
        .then((result) => {
            setTimeout(() => sendNotification(result.imagePath, userIdent, "update user photo"), 1000)
        })
        .then(function ( ) {
            const successBox = $(".successBox")
            successBox.empty()
            successBox.append(`
                            <div class="alert alert-info alert-dismissible fade show" role="alert">
                                 Your profile image is being uploaded, you can click <a href="account">here</a> to go back to account's page.
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
    });
}


// source: https://stackoverflow.com/a/18650828
function bytesToSize(bytes) {
    const sizes = ["Bytes", "KB", "MB", "GB", "TB"];

    if (bytes === 0) {
        return "0 bytes";
    }

    const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));

    return Math.round(bytes / Math.pow(1024, i), 2) + " " + sizes[i];
}