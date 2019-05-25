import std.algorithm;
import std.array;
import std.range;
import std.regex;
import std.stdio;
import std.string;
import std.uri;
import vibe.core.stream;
import vibe.inet.urltransfer;

enum SectionEnum {
    None,
    NotImplemented,
    DraftTasks,
    RequireAttention,
    NotConsidered,
    EndOfList,
}

string[][string] tasks;

void initialize() {
    tasks["Active_object"] ~= "1";
    tasks["Binary_digits"] ~= "1";
    tasks["Box_the_compass"] ~= "1";
    tasks["Factorial"] ~= "1";
    tasks["Fibonacci_sequence"] ~= "1";
    tasks["FizzBuzz"] ~= "1";
    tasks["Integer_sequence"] ~= "1";
    tasks["Knuth's_power_tree"] ~= "1";
    tasks["Logical_operations"] ~= "1";
    tasks["UTF-8_encode_and_decode"] ~= "1";
    //----------------------------------------------------------------
    tasks["Abbreviations,_automatic"] ~= "2";
    tasks["Abundant,_deficient_and_perfect_number_classifications"] ~= "2";
    tasks["Addition_chains"] ~= "2";
    tasks["Almost_prime"] ~= "2";
    tasks["Angle_difference_between_two_bearings"] ~= "2";
    tasks["Append_a_record_to_the_end_of_a_text_file"] ~= "2";
    tasks["Apply_a_digital_filter_(direct_form_II_transposed)"] ~= "2";
    tasks["Arithmetic_coding/As_a_generalized_change_of_radix"] ~= "2";
    tasks["Arithmetic-geometric_mean/Calculate_Pi"] ~= "2";
    tasks["Averages/Pythagorean_means"] ~= "2";
    tasks["Babbage_problem"] ~= "2";
    tasks["Bacon_cipher"] ~= "2";
    tasks["Base58Check_encoding"] ~= "2";
    tasks["Bilinear_interpolation"] ~= "2";
    tasks["Caesar_cipher"] ~= "2";
    tasks["Calculating_the_value_of_e"] ~= "2";
    tasks["Cantor_set"] ~= "2";
    tasks["Card_shuffles"] ~= "2";
    tasks["Cartesian_product_of_two_or_more_lists"] ~= "2";
    tasks["Catalan_numbers"] ~= "2";
    tasks["Chat_server"] ~= "2";
    tasks["Check_output_device_is_a_terminal"] ~= "2";
    tasks["Chinese_remainder_theorem"] ~= "2";
    tasks["Chinese_zodiac"] ~= "2";
    tasks["Cipolla's_algorithm"] ~= "2";
    tasks["Circles_of_given_radius_through_two_points"] ~= "2";
    tasks["Continued_fraction/Arithmetic/Construct_from_rational_number"] ~= "2";
    tasks["Convex_hull"] ~= "2";
    tasks["Create_a_file_on_magnetic_tape"] ~= "2";
    tasks["CUSIP"] ~= "2";
    tasks["Cycle_detection"] ~= "2";
    tasks["Damm_algorithm"] ~= "2";
    tasks["Data_Encryption_Standard"] ~= "2";
    tasks["Department_Numbers"] ~= "2";
    tasks["Determine_if_two_triangles_overlap"] ~= "2";
    tasks["Digital_root"] ~= "2";
    tasks["Display_a_linear_combination"] ~= "2";
    tasks["Dot_product"] ~= "2";
    tasks["Eertree"] ~= "2";
    tasks["Egyptian_division"] ~= "2";
    tasks["Egyptian_fractions"] ~= "2";
    tasks["Emirp_primes"] ~= "2";
    tasks["Euler_sum_of_powers"] ~= "2";
    tasks["Even_or_odd"] ~= "2";
    tasks["Faulhaber's_formula"] ~= "2";
    tasks["Faulhaber's_triangle"] ~= "2";
    tasks["Feigenbaum_constant_calculation"] ~= "2";
    tasks["Find_the_intersection_of_a_line_with_a_plane"] ~= "2";
    tasks["Find_the_intersection_of_two_lines"] ~= "2";
    tasks["Fivenum"] ~= "2";
    tasks["Floyd-Warshall_algorithm"] ~= "2";
    tasks["Floyd's_triangle"] ~= "2";
    tasks["Fork"] ~= "2";
    tasks["Four_Squares_Puzzle"] ~= "2";
    tasks["General_FizzBuzz"] ~= "2";
    tasks["Get_system_command_output"] ~= "2";
    tasks["Handle_a_signal"] ~= "2";
    tasks["Hello_world/Newline_omission"] ~= "2";
    tasks["Horner's_rule_for_polynomial_evaluation"] ~= "2";
    tasks["I_before_E_except_after_C"] ~= "2";
    tasks["Imaginary_base_numbers"] ~= "2";
    tasks["Integer_roots"] ~= "2";
    tasks["Jewels_and_Stones"] ~= "2";
    tasks["Josephus_problem"] ~= "2";
    tasks["Just_in_time_processing_on_a_character_stream"] ~= "2";
    tasks["Kahan_summation"] ~= "2";
    tasks["Kaprekar_numbers"] ~= "2";
    tasks["Kolakoski_sequence"] ~= "2";
    tasks["Kosaraju"] ~= "2";
    tasks["Leap_year"] ~= "2";
    tasks["Leonardo_numbers"] ~= "2";
    tasks["Longest_common_prefix"] ~= "2";
    tasks["Longest_Common_Substring"] ~= "2";
    tasks["Lucky_and_even_lucky_numbers"] ~= "2";
    tasks["Magic_squares_of_doubly_even_order"] ~= "2";
    tasks["Make_directory_path"] ~= "2";
    tasks["Markov_chain_text_generator"] ~= "2";
    tasks["Mersenne_primes"] ~= "2";
    tasks["Modular_arithmetic"] ~= "2";
    tasks["Montgomery_reduction"] ~= "2";
    tasks["Morse_code"] ~= "2";
    tasks["Multi-dimensional_array"] ~= "2";
    tasks["Multiplicative_order"] ~= "2";
    tasks["Munchausen_numbers"] ~= "2";
    tasks["N-body_problem"] ~= "2";
    tasks["Narcissist"] ~= "2";
    tasks["Negative_base_numbers"] ~= "2";
    tasks["Old_lady_swallowed_a_fly"] ~= "2";
    tasks["Orbital_elements"] ~= "2";
    tasks["Palindrome_detection"] ~= "2";
    tasks["Parallel_Brute_Force"] ~= "2";
    tasks["Parsing/Shunting-yard_algorithm"] ~= "2";
    tasks["Particle_Swarm_Optimization"] ~= "2";
    tasks["Partition_an_integer_X_into_N_primes"] ~= "2";
    tasks["Pascal's_triangle"] ~= "2";
    tasks["Polynomial_long_division"] ~= "2";
    tasks["Polynomial_regression"] ~= "2";
    tasks["Prime_conspiracy"] ~= "2";
    tasks["Proper_divisors"] ~= "2";
    tasks["Perfect_shuffle"] ~= "2";
    tasks["Permutation_test"] ~= "2";
    tasks["Pernicious_numbers"] ~= "2";
    tasks["P-value_correction"] ~= "2";
    tasks["Pythagorean_quadruples"] ~= "2";
    tasks["Quine"] ~= "2";
    tasks["Ramer-Douglas-Peucker_line_simplification"] ~= "2";
    tasks["Ranking_methods"] ~= "2";
    tasks["Readline_interface"] ~= "2";
    tasks["Recaman's_sequence"] ~= "2";
    tasks["Reflection/List_methods"] ~= "2";
    tasks["Repeat"] ~= "2";
    tasks["Resistor_mesh"] ~= "2";
    tasks["Reverse_a_string"] ~= "2";
    tasks["Safe_addition"] ~= "2";
    tasks["Sailors,_coconuts_and_a_monkey_problem"] ~= "2";
    tasks["Sattolo_cycle"] ~= "2";
    tasks["Set_of_real_numbers"] ~= "2";
    tasks["Shoelace_formula_for_polygonal_area"] ~= "2";
    tasks["Short-circuit_evaluation"] ~= "2";
    tasks["Sierpinski_pentagon"] ~= "2";
    tasks["Smith_numbers"] ~= "2";
    tasks["Snake_And_Ladder"] ~= "2";
    tasks["Sort_three_variables"] ~= "2";
    tasks["Split_a_character_string_based_on_change_of_character"] ~= "2";
    tasks["Stream_Merge"] ~= "2";
    tasks["Subleq"] ~= "2";
    tasks["Subset_sum_problem"] ~= "2";
    tasks["Substitution_Cipher"] ~= "2";
    tasks["Suffix_tree"] ~= "2";
    tasks["Sum_to_100"] ~= "2";
    tasks["Test_integerness"] ~= "2";
    tasks["Text_between"] ~= "2";
    tasks["The_Name_Game"] ~= "2";
    tasks["Thue-Morse"] ~= "2";
    tasks["Tokenize_a_string_with_escaping"] ~= "2";
    tasks["Tonelli-Shanks_algorithm"] ~= "2";
    tasks["Towers_of_Hanoi"] ~= "2";
    tasks["Trigonometric_functions"] ~= "2";
    tasks["Two_Sum"] ~= "2";
    tasks["Type_detection"] ~= "2";
    tasks["Unicode_strings"] ~= "2";
    tasks["Validate_International_Securities_Identification_Number"] ~= "2";
    tasks["Van_der_Corput_sequence"] ~= "2";
    tasks["Vector"] ~= "2";
    tasks["Vector_products"] ~= "2";
    tasks["Water_collected_between_towers"] ~= "2";
    tasks["Word_count"] ~= "2";
    tasks["Word_search"] ~= "2";
    tasks["Write_entire_file"] ~= "2";
    tasks["Write_language_name_in_3D_ASCII"] ~= "2";
    tasks["Write_to_Windows_event_log"] ~= "2";
    tasks["Zeckendorf_arithmetic"] ~= "2";
}

void harvest(string lang) {
    string language = lang.decodeComponent;
    stderr.writeln("Harvesting data for ", language);

    download("http://rosettacode.org/wiki/Reports:Tasks_not_implemented_in_" ~ lang, (scope input) {
        auto builder = appender!string;
        try {
            ubyte[128] buf;
            int len;
            do {
                len = input.read(buf, IOMode.once);
                if (len > 0) {
                    if (len == buf.length) {
                        builder ~= cast(string) buf;
                    } else {
                        builder ~= cast(string) buf[0 .. len];
                    }
                }
            } while (!input.empty);
        } catch (Exception e) {
            // Do not know what to do about this
            // stderr.writeln("[ERROR]", e.msg);
        }

        //<span class="mw-headline" id="Not_implemented">Not implemented</span>
        //<span class="mw-headline" id="Draft_tasks_without_implementation">Draft tasks without implementation</span>
        //<span class="mw-headline" id="Requiring_Attention">Requiring Attention</span>
        //<span class="mw-headline" id="Not_Considered">Not Considered</span>
        //<span class="mw-headline" id="End_of_List">End of List</span>
        auto headRegex = ctRegex!(`<span class="mw-headline" id="([^"]+)">[^<]+</span>`);

        //<li><a href="/wiki/15_puzzle_solver" title="15 puzzle solver">15 puzzle solver</a></li>
        auto taskRegex = ctRegex!(`<li><a href="/wiki/([^"]+)" title="[^"]+">[^<]+</a></li>`);

        string source = builder.data;
        SectionEnum section = SectionEnum.None;
        foreach (line; source.lineSplitter) {
            auto headMatch = matchFirst(line, headRegex);
            if (headMatch) {
                string head = headMatch[1];
                switch(head) {
                    case "Not_implemented":
                        section = SectionEnum.NotImplemented;
                        break;
                    case "Draft_tasks_without_implementation":
                        section = SectionEnum.DraftTasks;
                        break;
                    default:
                        section = SectionEnum.None;
                        break;
                }
            }

            if (section == SectionEnum.NotImplemented
             || section == SectionEnum.DraftTasks) {
                auto taskMatch = matchFirst(line, taskRegex);
                if (taskMatch) {
                    string task = taskMatch[1].decodeComponent;
                    string tl;
                    switch(language) {
                        case "C_sharp":
                            tl = "C#";
                            break;
                        case "F_sharp":
                            tl = "F#";
                            break;
                        default:
                            tl = language;
                            break;
                    }
                    if (task !in tasks || tasks[task][$-1] != tl) {
                        if (isNumeric(task)) {
                            tasks[task] ~= tl;
                        } else {
                            tasks[task] ~= tl;
                        }
                    }
                }
            }
        }
    });
}

void main() {
    initialize();

    auto langArr = [
        "C",
        "C_sharp",
        "C%2B%2B",
        "D",
        "F_Sharp",
        // "Forth",
        "Go",
        "Java",
        "Kotlin",
        "Lua",
        "Modula-2",
        "Pascal",
        "Perl",
        "Python",
        "Ruby",
        "Visual Basic .NET",
    ];

    foreach (lang; langArr) {
        harvest(lang);
    }

    stderr.writeln("Task Summary");
    foreach (k; tasks.keys.sort) {
        auto taskName = k;
        auto langs = tasks[k];

        if (isNumeric(taskName)) {
            taskName = "'" ~ taskName;
        }

        if (langs[0] == "1") {
            if (langs.length > 1) {
                writeln("1|", taskName, "|", tasks[k][1..$]);
            }
        } else if (langs[0] == "2") {
            if (langs.length > 1) {
                writeln("2|", taskName, "|", tasks[k][1..$]);
            }
        } else if (tasks[k].length > 1) {
            writeln("3|", taskName, "|", tasks[k]);
        } else {
            writeln("4|", taskName, "|", tasks[k]);
        }
    }
}
