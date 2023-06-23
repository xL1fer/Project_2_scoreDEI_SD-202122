/*
    Events select javascript handler
*/

// execute function on window load aswell
window.onload = function() {
    EventHandler();
};

function EventHandler() {

    let select = document.getElementById('event-handler');
    let value = select.options[select.selectedIndex].value;

    dynamicdropdown(value);
    
    let event_goal_foul = document.getElementById('event-goal-foul');
    let event_substitution_in = document.getElementById('event-substitution-in');
    let event_substitution_out = document.getElementById('event-substitution-out');

    console.log(value);

    if (value == 1 || value == 3) {
        event_goal_foul.style.display = 'table-row';
    }
    else {
        event_goal_foul.style.display = 'none';
    }

    if (value == 4) {
        event_substitution_in.style.display = 'table-row';
        event_substitution_out.style.display = 'table-row';
    }
    else {
        event_substitution_in.style.display = 'none';
        event_substitution_out.style.display = 'none';
    }

}

/*
    Responsive event description
*/

function doHTML(list) {
    let string ="";
    let index = 0;
    list.forEach(element => {
        string += `<option value="${++index}">${element}</option>`;
    });
    return string;
}

function dynamicdropdown(val) {
    let options1 = ["Normal", "Penalty", "Free kick"];
    let options2 = ["Start", "Stopped","Resumed", "Finish"];
    let options3 = ["Yellow", "Red"];
    let options4 = ["1", "2", "3", "4", "5"];

    let description_options = document.getElementById("description-options");
    if (val==1){
        description_options.innerHTML = doHTML(options1);
    }
    else if (val==2){
        description_options.innerHTML = doHTML(options2);
    }
    else if (val==3){
        description_options.innerHTML = doHTML(options3);
    }
    else if (val==4){
        description_options.innerHTML = doHTML(options4);
    }
}