#include <algorithm>
#include <iostream>
#include <fstream>
#include <map>
#include <regex>
#include <string>
#include <vector>

int main() {
    using namespace std;
    regex wordRgx("\\w+");
    map<string, int> freq;
    string line;

    ifstream in("135-0.txt");
    if (!in.is_open()) {
        cerr << "Failed to open file\n";
        return 1;
    }
    while (getline(in, line)) {
        auto words_itr = sregex_iterator(line.cbegin(), line.cend(), wordRgx);
        auto words_end = sregex_iterator();
        while (words_itr != words_end) {
            auto match = *words_itr;
            auto word = match.str();
            if (word.size() > 0) {
                auto entry = freq.find(word);
                if (entry != freq.end()) {
                    entry->second++;
                } else {
                    freq.insert(make_pair(word, 1));
                }
            }
            words_itr = next(words_itr);
        }
    }
    in.close();

    vector<pair<string, int>> pairs;
    for (auto iter = freq.cbegin(); iter != freq.cend(); ++iter) {
        pairs.push_back(*iter);
    }
    sort(pairs.begin(), pairs.end(), [=](pair<string, int>& a, pair<string, int>& b) {
        return a.second > b.second;
    });

    cout << "Rank  Word  Frequency\n";
    cout << "====  ====  =========\n";
    int rank = 1;
    for (auto iter = pairs.cbegin(); iter != pairs.cend() && rank <= 10; ++iter) {
        printf("%2d   %4s   %5d\n", rank++, iter->first.c_str(), iter->second);
    }

    return 0;
}
