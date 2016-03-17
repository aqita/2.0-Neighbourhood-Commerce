
function checkout(){
      var count = $("#todolist li").length;
      items = [];
      for(i=1;i<=count;i=i+1){
        items.push($( "#todolist li:nth-child("+i+")" ).text());
      }
      console.log(items);
      placeorder();
}
function placeorder(){
    if(!getCookie("userId")){
      window.location.replace("login.html");
    }
    if(getCookie("name")!=="undefined"){name_var = getCookie("name")}
    if(getCookie("phone")!=="undefined"){phone_var = getCookie("phone")}
    if(getCookie("address1")!=="undefined"){add1_var =  getCookie("address1")}
    if(getCookie("address2")!=="undefined"){add2_var =  getCookie("address2")}
    if(getCookie("landmark")!=="undefined"){land_var =  getCookie("landmark")}
    var dt = new Date();
    var typeorder = "Order Delivery";
    var div = "<section id='intro' class='order_id_id' style='margin-top:20px;text-align: left;'>"+
              "<div style='padding-bottom: 30px;width:100%;padding-left:10px;'>"+
              "<div class='form-group' style='margin-bottom: 5px;text-align: left;'>"+
              "<h5 style='margin-top: 0px;margin-bottom: 15px;font-weight:600;padding-left: 10px;font-size: 16px;'>Select Date and Time for "+typeorder+"</h5><div style='padding-left: 10px;'><span style='font-weight:600;'>Date : </span><input data-view='Dropdown' style='width: 35%;height: 25px;' id='date_id' type='text' data-field='date' readonly>"+
              "<br><br><span style='font-weight:600;'>Time : </span><input data-view='Dropdown' id='time_id' style='width: 35%;height: 25px;' type='text' data-field='time' readonly></div>"+
              "<div id='dtBox'></div></div><div class='form-group'>"+
              "</div></div><span style='text-align: left;font-size: 16px;font-weight: 600;padding-left: 20px;'>Contact Details</span>"+
              "<div style='width:90%;padding-left:22px;padding-top:10px;'>"+
              "<div class='form-group' style='margin-bottom: 5px;'>"+
              "<input type='text' class='form-control' id='name_id' placeholder='Name' value='"+name_var+"'>"+
              "</div><div class='form-group' style='margin-bottom: 5px;'>"+
              "<input type='text' class='form-control' id='phone_id' placeholder='Phone Number' value='"+phone_var+"'></div>"+
              "<div class='form-group' style='margin-bottom: 5px;'>"+
              "<input type='text' class='form-control' id='add1_id' placeholder='Address Line 1' value='"+add1_var+"'></div>"+
              "<div class='form-group' style='margin-bottom: 5px;'>"+
              "<input type='text' class='form-control' id='add2_id' placeholder='Address Line 2' value='"+add2_var+"'></div>"+
              "<div class='form-group' style='margin-bottom: 5px;'>"+
              "<input type='text' class='form-control' id='landmark_id' placeholder='Landmark' value='"+land_var+"'></div>"+
              "<button type='button' class='btn btn-warning' style='margin-top:20px;' onclick='confirmorder();'>Confirm</button></div></section>"; 
              $( "#home" ).children().hide();
              $( "#home" ).prepend( div );
              $("#dtBox").DateTimePicker();
              items = items.toString();
}
function confirmorder(){
  update_once = 1;
 var date_id = $("#date_id").val();
 var time_id = $("#time_id").val();
 var name_id = $("#name_id").val();
 var phone_id = $("#phone_id").val();
 var add1_id = $("#add1_id").val();
 var add2_id = $("#add2_id").val();
 var landmark_id = $("#landmark_id").val();
 var useid = getCookie("userId");
 var storId = getCookie("store_id");
 var typ = getCookie("typ");
 var delivery = 1;
 console.log(date_id,time_id,name_id,phone_id,add1_id,add2_id,add2_id,landmark_id,typ);
 if(typ == "l"){
   $.get( "http://52.76.118.161:7070/api/v1/placeOrder",{
        userid:useid, 
        storeid:storId, 
        type:typ, 
        homedelivery:delivery, 
        date:date_id, 
        time:time_id, 
        name:name_id, 
        phone:phone_id, 
        addressline1:add1_id, 
        addressline2:add2_id, 
        landmark:landmark_id, 
        item:items
      }, function( data ) {
        var data = JSON.parse(data);
        if(data = true){
          var mess = "Hi! <br>This is an enquiry. Please confirm price. The details are<br>"+"Name:"+name_id+"<br> Phone:"+phone_id+"<br> Order:"+items+"<br> Date:"+date_id+"<br> Time:"+time_id+"<br>Address:<br>"+add1_id+",<br>"+add2_id+"<br>Landmark:"+landmark_id;
          $applozic.fn.applozic('sendMessage', {to:storId,message:mess});
          $applozic.fn.applozic('loadTab', storeId);
          $("#chat_id_tab").attr('class', 'active');
          $("#chat_id_tab").click();
          $("#order_id_tab").attr('class', 'hhhh');
          $( "#home" ).children().hide();
          
        }

   });
setCookie("address1", add1_id, 30);
setCookie("address2", add2_id, 30);
setCookie("landmark", landmark_id, 30);
}
else if(typ == "s"){
    $.get( "http://52.76.118.161:7070/api/v1/placeOrder",{
            userid:useid, 
            storeid:storId, 
            type:typ, 
            homedelivery:delivery, 
            date:date_id, 
            time:time_id, 
            name:name_id, 
            phone:phone_id, 
            addressline1:add1_id, 
            addressline2:add2_id, 
            landmark:landmark_id, 
          }, function( data ) {
            var data = JSON.parse(data);
            if(data = true){
              alert("Service Placed");
              var mess = "Hi! <br>You have a new order. The details are<br>"+"Name:"+name_id+"<br> Phone:"+phone_id+"<br> Date:"+date_id+"<br> Time:"+time_id+"<br>Address:<br>"+add1_id+"<br>"+add2_id+"<br>Landmark:"+landmark_id;
              $applozic.fn.applozic('sendMessage', {to:storId,message:mess});
              $applozic.fn.applozic('loadTab', storeId);
              $("#chat_id_tab").attr('class', 'active');
              $("#chat_id_tab").click();
              $("#order_id_tab").attr('class', 'hhhh');
              $( "#home" ).children().hide();
            }

       });
    }

}
function order_click(){
  $( "#home" ).children().show();
  $(".order_id_id").children().hide();
}

function loadproducts(){
  if(getCookie("typ") == "l"){listview();}
    else if(getCookie("typ") == "n"){
            $("#order_id_tab").hide();
            $("#details_id_tab").css("width","50%");
            $("#chat_id_tab").css("width","50%");
            $("#details_click").click();

        }
    else{
    $.get( "http://52.76.118.161:7070/api/v1/getOfferings",{storeid:storeId}, function( data ) {
        var products = JSON.parse(data);
        var num = products.length;
        if(getCookie("typ") == "p"){
            for(i=0;i<num;i++){
                var offerId = products[i].offerId;
                var name = products[i].name;
                var qty = products[i].qty;
                var price = products[i].price;
                var uom = products[i].uom;
                add_products(storeId,name,qty,price,uom,offerId);
            }
        }
        else if(getCookie("typ") == "s"){
          for(i=0;i<num;i++){
            var offerId = products[i].offerId;
            var name = products[i].name;
            var price = products[i].price;
            add_services(storeId,name,price,offerId);
          }
        }
    });
}
}
function add_services(storeId,name,price,offerId){
  var div = "<div class='container' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;height: 65px;padding-left:10px;'><span id='"+storeId+"'></span><span id='"+offerId+"'></span><div style='width:100%;padding:10px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;'><div style='float:left;'><span style='font-size:16px;float:left;'>"+name+"</span></div><div style='width:30%;float:right;font-size:14px;padding-right:20px;margin-top:3px;'><span>Rs "+price+"</span></div></div></div>";
    $( "#home" ).prepend( div );
    $("#rs_id").hide();
    $("#button_id").css("width", "50%");
    $("#button_id").html("Book");
    $("#button_id").css("font-weight", "600");
    $("#button_id").css("font-size", "16px");

}

function add_products(storeId,name,qty,price,uom,offerId){
    var div = "<div class='container' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;padding-top:15px;' id='"+offerId+"'><span></span><div style='float:left;padding-left:10px;text-align: left;font-weight: 600;'>"+name+"<br><small>"+qty+uom+"</small><br><small>Rs "+price+"</small></div><div style='float:right;'><div id='slect' class='container'><select class='form-control' style='margin-top:15px;' onchange=addproduct("+storeId+","+offerId+",'"+name+"',"+qty+",'"+uom+"',"+price+");><option value='0'>0</option><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option><option value='4'>4</option></select></div>";
    $( "#home" ).prepend( div );
}
function addproduct(storeId,offerId,name,qty,uom,price){
    console.log(storeId,offerId,name,qty,uom,price);
    var gg = $("#"+offerId).find("select").val();
    cost = cost+(gg*price);
    var item = [offerId, name, qty, uom, gg, price];
    items.push(item);
    list[offerId] = 1;
    $("#cost").html(cost);
}
function listview(){
  div3 = "<section ><ul id='todolist' style='margin-left:-7px;position: static;margin-top:20px;'></ul><form action='#' method='post' margin-bottom:50px;><div style='margin-top:20px;'><input type='text' name='newitem' id='newitem' style='width: 50%;margin-right:4%;' placeholder='Add new item'><button type='submit' class='btn btn-warning' style='margin-left:0px;width: 30%;' onclick='ggg();''>Add item</button></div></form></section>";
  storetype = "list"; 
  $( "#home" ).prepend( div3 );
  $("#rs_id").hide();
  $("#button_id").css("width", "50%");
  $("#button_id").css("font-weight", "600");
  $("#button_id").css("font-size", "16px");



}
function ggg(){
  var todo = document.querySelector( '#todolist' ),
      form = document.querySelector( 'form' ),
      field = document.querySelector( '#newitem' );
    
  form.addEventListener( 'submit', function( ev ) {
    var text = field.value;
    if ( text !== '' ) {
      todo.innerHTML += '<li>' + text + '</li>';
      field.value = '';
      field.focus();
    }
    ev.preventDefault();
  }, false);

  todo.addEventListener( 'click', function( ev ) {
    var t = ev.target;
    if ( t.tagName === 'LI' ) {
      if ( t.classList.contains( 'done' ) ) {
        t.parentNode.removeChild( t );
      } else {  
        t.classList.add( 'done' );
      }
    };
    ev.preventDefault();
  }, false);
}

function arraycheck(offerId, array){
   var num = items.length;
   for(i=0;i<=num; i=i+1){
      if(array[i][0] == offerId){
        return true;
        break;
      }
   }
   return false;
   var todo = document.querySelector( '#todolist' ),
        form = document.querySelector( 'form' ),
        field = document.querySelector( '#newitem' );
      
    form.addEventListener( 'submit', function( ev ) {
      var text = field.value;
      if ( text !== '' ) {
        todo.innerHTML += '<li>' + text + '</li>';
        field.value = '';
        field.focus();
      }
      ev.preventDefault();
    }, false);

    todo.addEventListener( 'click', function( ev ) {
      var t = ev.target;
      if ( t.tagName === 'LI' ) {
        if ( t.classList.contains( 'done' ) ) {
          t.parentNode.removeChild( t );
          console.log("2");
        } else {  
          t.classList.add( 'done' );
          console.log("3");
        }
      };
      ev.preventDefault();
    }, false);
}

function storedetails(){
   $.get( "http://52.76.118.161:7070/api/v1/getStoreDetails",{storeid:storeId}, function( data ) {
        var products = JSON.parse(data);
        var url = 'img/backdrop.jpg';
         name = products.name;
         tags = products.tags;
        $("#tags_id_id").html(tags);
        var timing = products.timing;
        var description = products.description;
        var servicearea = products.servicearea;
        var rating = products.rating;
        var address = products.address;
        var homedelivery = products.homedelivery;
        $("#logo").html(name);
        var div = "<div id='slect' class='container' style='border: 1px solid #ddd;padding-bottom:20px; background-size: cover; background-image: url("+url+");height:150px;'><div style='margin-top:40px;color:white;'><h3>"+name+"</h3></div><div style='float:left;    padding-left: 10px;color:white;margin-top:20px;width:100%;text-align: left;'>"+timing+" <div style='float:right;padding-right: 20px;'>"+rating+"</div></div></div>";
        if(homedelivery){
          var div2 = "<div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><div style='width:100%;padding:10px;padding-bottom: 0px;margin-bottom:20px;text-align:left;'><div style='float:left;'><strong style='float:left;'><span style='font-size:16px;font-weight:600;'>Home delivery is available</small></strong></div></div></div>";
          $( "#store_list" ).prepend( div2 );
          var div2 = "<div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><div style='width:100%;padding:10px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;text-align:left;'><div style='float:left;'><strong style='float:left;'><span style='font-size:16px;font-weight:600;'>Service Area</span> <br><small style='font-weight:500;color: #525252;'>"+servicearea+"</small></strong></div></div></div>";
        $( "#store_list" ).prepend( div2 );
        }
        else{
//          var div2 = "<div class='container' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><div style='width:100%;padding:10px;padding-top:0px;margin-bottom:20px;text-align:left;'><div style='float:left;'><strong style='float:left;'><span style='font-size:16px;font-weight:600;'>Home delivery is not available</small></strong></div></div></div>";
//            $( "#store_list" ).prepend( div2 );
        }
        $( "#details" ).prepend( div );
        var div2 = "<div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><div style='width:100%;padding:10px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;text-align:left;'><div style='float:left;'><strong style='float:left;'><span style='font-size:16px;font-weight:600;'>Description</span> <br><small style='font-weight:500;color: #525252;'>"+description+"</small></strong></div></div></div>";
        $( "#store_list" ).prepend( div2 );
        var div2 = "<div class='container card' style='border:1px solid #eee;margin-bottom:10px;padding-bottom:15px;'><div style='width:100%;padding:10px;padding-top:20px;padding-bottom:30px;margin-bottom:20px;text-align:left;'><div style='float:left;'><strong style='float:left;'><span style='font-size:16px;font-weight:600;'>Address</span> <br><small style='font-weight:500;color: #525252;'>"+address+"</small></strong></div></div></div>";
        $( "#store_list" ).prepend( div2 );
      });


}
function chat_click(){
  if(!getCookie("userId")){
    window.location.replace("login.html");
  }
  $applozic.fn.applozic('loadTab', storeId);
}

 //Sample json contains display name and photoLink for userId
            var contacts = {storeId: {"displayName": name, "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}, "user2": {"displayName": "Adarsh", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}, "user3": {"displayName": "Shanki", "photoLink": "http://applozic.appspot.com/resources/images/applozic_icon.png"}};
            function readMessage(userId) {
                //console.log(userId);
            }

            //Callback function to return display name by userId 
            function displayName(userId) {
                //Todo: replace this with users display name
                var contact = contacts[userId];   // contacts sample given above
                if (typeof contact !== 'undefined') {
                    return contact.displayName;
                }
            }

            //Callback function to return contact image url by userId 
            function contactImageSrc(userId) {
                var contact = contacts[userId]; // contacts sample given above
                if (typeof contact !== 'undefined') {
                    return contact.photoLink;
                }
            }
            //callback function execute after plugin initialize. 
            function onInitialize() {
              $(".se-pre-con").fadeOut("slow");
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
    storeId = getCookie("store_id");
    cost = 0;
    items = [];
    list = [];
    loadproducts();
    storedetails();
    var typ = getCookie("typ");
    if(typ == "s"){
      $("#rs_id").empty();
      $("#order_id").html("Enquire");
    }
    

});
