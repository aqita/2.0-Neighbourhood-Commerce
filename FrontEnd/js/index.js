function login(){
    var username = $('#login-username').val();
    var pass = $('#login-password').val();
    if(username && pass){
       $.get( "http://52.76.118.161:7070/api/v1/verifyUser",{email:username, password:pass}, function( data ) {
              data2 = JSON.parse(data);
              if(data2.authenticated == true){
                  var json_data = JSON.parse(data);
                  document.cookie = json_data.storeId;
                  setCookie("userId", json_data.id, 30);
                  setCookie("name", json_data.name, 30);
                  setCookie("phone", json_data.phone, 30);
                  setCookie("address1", json_data.addressline1, 30);
                  setCookie("address2", json_data.addressline2, 30);
                  setCookie("landmark", json_data.landmark, 30);
                  usr = getCookie("userId");
                  nam = getCookie("name");
                  location.reload();
                  $applozic.fn.applozic('loadTab');
                  $("#chat").empty();
              }
              else{
                $("#error").html("Invalid Email or Password");
              }
          });
    }
    else{
      $("#error").html("Please enter all fields");
    }
}
function loadstore(storeId, type){
    if(type == "s"){
       setCookie("store_id", storeId, 30);
       setCookie("typ", type, 30);
       window.location.href = "product.html";
    }
    else if(type == "p"){
      setCookie("store_id", storeId, 30);
      setCookie("typ", type, 30);
       window.location.href = "product.html";
    }
    else if(type == "l"){
      setCookie("store_id", storeId, 30);
      setCookie("typ", type, 30);
       window.location.href = "product.html";
    }
    else if(type == "n"){
      setCookie("store_id", storeId, 30);
      setCookie("typ", type, 30);
       window.location.href = "product.html";
    }
}
function add_store(storeId, name, tags, area, category, type ){
  div = "<a href='#' onclick=loadstore("+storeId+",'"+type+"');><div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><span style='display:none;' id='"+storeId+"'></span><span style='display:none;' id='"+type+"'></span><span style='display:none;' id='"+category+"'></span><div style='width:100%;padding:10px;    padding-left: 4px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;'><div style='float:left;'><strong style='font-size:12px;float:left;'>"+name+"</strong><br><small style='display:block;'><strong style='color:#777;left: 20px;float:left; padding-top: 6px;font-size:10px;'>"+tags+"</strong></small></div><div style='width:30%;float:right;font-size:12px;padding-right:20px;margin-top:3px;'><strong style='color:#20B2AA'>"+area+"</strong></div></div></div></a>";
  $( "#store_list" ).append( div );

}
var are = "";
var ipa = "";
function storedetails(){
      $.get( "http://52.76.118.161:7070/api/v1/getNearbyStores",{area:are, ip:ipa}, function( data ) {
              data3 = JSON.parse(data);
              var num_stores = data3.length;
              divFirst = "<a href='#' onclick=loadstore('195','n');><div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><span style='display:none;' id='195'></span><span style='display:none;' id='n'></span><span style='display:none;' id=''></span><div style='width:100%;padding:10px;    padding-left: 4px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;'><div style='float:left;'><strong style='font-size:12px;float:left;'>Aqita</strong><br><small style='display:block;'><strong style='color:#777;left: 20px;float:left; padding-top: 6px;font-size:10px;'>Chat with us!</strong></small></div><div style='width:30%;float:right;font-size:12px;padding-right:20px;margin-top:3px;'><strong style='color:#20B2AA'>Bengaluru</strong></div></div></div></a>";
  			  $( "#store_list" ).append( divFirst );
              for(i=0;i<num_stores;i++){
                  var storeId = data3[i].storeId;
                  var name = data3[i].name;
                  var tags = data3[i].tags;
                  var area = data3[i].area;
                  var category = data3[i].category;
                  var type = data3[i].type;
                  add_store(storeId,name,tags,area,category,type);
              }
      });
}
function filter(){
  $( "#store_list" ).empty();
  divFirst = "<a href='#' onclick=loadstore('195','n');><div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><span style='display:none;' id='195'></span><span style='display:none;' id='n'></span><span style='display:none;' id=''></span><div style='width:100%;padding:10px;    padding-left: 4px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;'><div style='float:left;'><strong style='font-size:12px;float:left;'>Aqita</strong><br><small style='display:block;'><strong style='color:#777;left: 20px;float:left; padding-top: 6px;font-size:10px;'>Chat with us!</strong></small></div><div style='width:30%;float:right;font-size:12px;padding-right:20px;margin-top:3px;'><strong style='color:#20B2AA'>Ulsoor</strong></div></div></div></a>";
  $( "#store_list" ).append( divFirst );
  var num = data3.length;
  var cat = $( "#cate :selected" ).text();
   areaa = $("#area_id :selected").text();
  filterr = 0;
  for(i=0;i<num;i++){
      if(data3[i].category == cat && data3[i].area == areaa){
        filterr = 1;
        var storeId = data3[i].storeId;
        var name = data3[i].name;
        var tags = data3[i].tags;
        var area = data3[i].area;
        var category = data3[i].category;
        var type = data3[i].type;
        add_store(storeId,name,tags,area,category,type);
      }
      else if("--" == areaa && data3[i].category == cat){
        filterr = 2;
        var storeId = data3[i].storeId;
        var name = data3[i].name;
        var tags = data3[i].tags;
        var area = data3[i].area;
        var category = data3[i].category;
        var type = data3[i].type;
        add_store(storeId,name,tags,area,category,type);
              
      }
      else if("--" == cat && data3[i].area == areaa){
        filterr = 3;
        var storeId = data3[i].storeId;
        var name = data3[i].name;
        var tags = data3[i].tags;
        var area = data3[i].area;
        var category = data3[i].category;
        var type = data3[i].type;
        add_store(storeId,name,tags,area,category,type);
              
      }
      else if("--" == cat && "--" == areaa){
        filterr = 4;
        var storeId = data3[i].storeId;
        var name = data3[i].name;
        var tags = data3[i].tags;
        var area = data3[i].area;
        var category = data3[i].category;
        var type = data3[i].type;
        add_store(storeId,name,tags,area,category,type);
              
      }
  }
  if(filterr == 1){
  for(i=0;i<num;i++){
      if(data3[i].category == cat && data3[i].area !== areaa){
        var storeId = data3[i].storeId;
        var name = data3[i].name;
        var tags = data3[i].tags;
        var area = data3[i].area;
        var category = data3[i].category;
        var type = data3[i].type;
        add_store(storeId,name,tags,area,category,type);
      }
    }
  }
  else if(filterr == 2){
    for(i=0;i<num;i++){
      if(false){
          var storeId = data3[i].storeId;
          var name = data3[i].name;
          var tags = data3[i].tags;
          var area = data3[i].area;
          var category = data3[i].category;
          var type = data3[i].type;
          add_store(storeId,name,tags,area,category,type);
                
        }
    }
  }
  else if(filterr == 3){
    for(i=0;i<num;i++){
      if(data3[i].area !== areaa){
          var storeId = data3[i].storeId;
          var name = data3[i].name;
          var tags = data3[i].tags;
          var area = data3[i].area;
          var category = data3[i].category;
          var type = data3[i].type;
          add_store(storeId,name,tags,area,category,type);
                
        }
    }
  }
  else if(filterr == 0){
    for(i=0;i<num;i++){
      if(data3[i].category == cat){
          var storeId = data3[i].storeId;
          var name = data3[i].name;
          var tags = data3[i].tags;
          var area = data3[i].area;
          var category = data3[i].category;
          var type = data3[i].type;
          add_store(storeId,name,tags,area,category,type);
                
        }
    }
  }
}
function register(){
            var nam = $('#name').val();
            var email_r = $('#email_r').val();
            var pwd_r = $('#pwd_r').val();
            var c_pwd = $('#c_pwd').val();
            var tc = $("#tc_id:checkbox:checked");
            var gggg = tc.length;
            if(nam && email_r && pwd_r && c_pwd){
              if(gggg){
                $("#error").empty;
                if(pwd_r == c_pwd){

                 $.get( "http://52.76.118.161:7070/api/v1/createUser",{name:nam, email:email_r, password:pwd_r}, function( data ) {
                        var data2 = JSON.parse(data);
                        if(data2.successful == true){
                            var json_data = JSON.parse(data);
                            document.cookie = json_data.storeId;
                            setCookie("userId", json_data.userId, 30);
                            setCookie("name", json_data.name, 30);
                            $("#error").empty();
                            $("#modal_data").html("Thank you for Registering.");
                            $('#message').modal('show');
                            window.location.replace("index.html");
                            
                        }
                        else{
                          $("#error2").html("Email already in use");
                        }
                    });
               }
               else{
                    $("#error2").html("Passwords do not match");

               }
             }
             else{
                    $("#error2").html("Please agree to our Terms and Conditions");

               }
            }
            else{
              $("#error2").html("Please enter all fields");
            }
        }
var contacts = {storeId: {"displayName": name, "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}, "user2": {"displayName": "Adarsh", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}, "user3": {"displayName": "Shanki", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}};
function readMessage(userId) {
    //console.log(userId);
}

//Callback function to return display name by userId 
function displayName(userId) {
    return '';
}

//Callback function to return contact image url by userId 
function contactImageSrc(userId) {
    var contact = contacts[userId]; // contacts sample given above
    if (typeof contact !== 'undefined') {
        return contact.photoLink;
    }
}
//callback function execute after plugin initialize. 
function callback() {
  usr = getCookie("userId");
nam = getCookie("name");
window.applozic.init({userId: usr, userName: nam, appId: '2f5889429834046949516164f3617897', contactDisplayName: displayName, contactDisplayImage: contactImageSrc, onInit: onInitialize});
}
if(getCookie("userId") == ""){
usr = "fsaa";
nam = "sdsd";
}else{
   usr = getCookie("userId");
  nam = getCookie("name");
}

window.applozic.init({userId: usr, userName: nam, appId: '2f5889429834046949516164f3617897', contactDisplayName: displayName, contactDisplayImage: contactImageSrc, onInit: onInitialize});

// Note: maxAttachmentSize property value declared above is in MB

// var contactjson = {"contacts": [{"userId": "user1", "displayName": "Devashish", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}, {"userId": "user2", "displayName": "Adarsh", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}, {"userId": "user3", "displayName": "Shanki", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}]};
// To load contact list use below function and pass contacts json in format shown above in variable 'contactjson'. 
// $applozic.fn.applozic('loadContacts', contactjson);

function chatclick(){
  if(getCookie("userId")){
    $("#chat").empty();
    $applozic.fn.applozic('loadTab');
  }

}

function onInitialize(text) { 
  $(".se-pre-con").fadeOut("slow");
}

$("#login-password").keypress(function(e) {
    if(e.which == 13) {
    $("#login_btn").click();
    }
});
$("#c_pwd").keypress(function(e) {
    if(e.which == 13) {
    $("#sign_up").click();
    }
});

$(document).ready(function() {
  $.ripple(".btn", {
    debug: false,
    on: 'mousedown',
    opacity: 0.4, 
    color: "auto",
    multi: false,
    duration: 0.7,
    rate: function(pxPerSecond) {
        return pxPerSecond;
    },

    easing: 'linear'
});
  $.ripple(".ggg", {
    debug: false,
    on: 'mousedown',
    opacity: 0.4,
    color: "auto",
    multi: false,
    duration: 0.7,

    rate: function(pxPerSecond) {
        return pxPerSecond;
    },

    easing: 'linear'
});
  storedetails();
  $("#c_pwd").keypress(function(e) {
      if(e.which == 13) {
      $("#signup_btn").click();
      }
  });
  $("#login-password").keypress(function(e) {
      if(e.which == 13) {
      $("#login_btn").click();
      }
  });
});
